#include <stdio.h>
#include <elf.h>
#include <android/log.h>
#include <malloc.h>
#include <stdlib.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <cstring>
#include <unistd.h>
#include "SymbolFinder.h"

#define TAG "MSHook"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#define MAX_NAME_LEN 256
#define MEMORY_ONLY "[memory]"

struct mm {
    char name[MAX_NAME_LEN];
    unsigned long start, end;
};

struct symlist {
    Elf32_Sym *sym;
    char *str;
    unsigned num;
};

struct symtab {
    struct symlist *st;
    struct symlist *dyn;
};

typedef struct symtab *symtab_t;

static void *xmalloc(size_t size) {
    void *p = malloc(size);
    if (!p) {
        LOGE("Out of memory\n");
        exit(1);
    }
    return p;
}

static int my_pread(int fd, void *buf, size_t count, off_t offset) {
    if (lseek(fd, offset, SEEK_SET) == -1) return -1;
    return read(fd, buf, count);
}

static struct symlist *get_syms(int fd, Elf32_Shdr *symh, Elf32_Shdr *strh) {
    struct symlist *sl = (struct symlist *) xmalloc(sizeof(struct symlist));
    sl->str = NULL;
    sl->sym = NULL;

    if (symh->sh_size % sizeof(Elf32_Sym)) {
        free(sl);
        return NULL;
    }

    sl->num = symh->sh_size / sizeof(Elf32_Sym);
    sl->sym = (Elf32_Sym *) xmalloc(symh->sh_size);
    
    if (my_pread(fd, sl->sym, symh->sh_size, symh->sh_offset) != symh->sh_size) {
        free(sl->sym);
        free(sl);
        return NULL;
    }

    sl->str = (char *) xmalloc(strh->sh_size);
    if (my_pread(fd, sl->str, strh->sh_size, strh->sh_offset) != strh->sh_size) {
        free(sl->str);
        free(sl->sym);
        free(sl);
        return NULL;
    }

    return sl;
}

static int do_load(int fd, symtab_t symtab) {
    Elf32_Ehdr ehdr;
    if (read(fd, &ehdr, sizeof(ehdr)) != sizeof(ehdr)) {
        return -1;
    }

    if (memcmp(ELFMAG, ehdr.e_ident, SELFMAG) != 0) {
        return -1;
    }

    size_t shdr_size = ehdr.e_shentsize * ehdr.e_shnum;
    Elf32_Shdr *shdr = (Elf32_Shdr *) xmalloc(shdr_size);
    
    if (my_pread(fd, shdr, shdr_size, ehdr.e_shoff) != shdr_size) {
        free(shdr);
        return -1;
    }

    char *shstrtab = (char *) xmalloc(shdr[ehdr.e_shstrndx].sh_size);
    if (my_pread(fd, shstrtab, shdr[ehdr.e_shstrndx].sh_size, 
                 shdr[ehdr.e_shstrndx].sh_offset) != shdr[ehdr.e_shstrndx].sh_size) {
        free(shstrtab);
        free(shdr);
        return -1;
    }

    Elf32_Shdr *symh = NULL, *dynsymh = NULL;
    Elf32_Shdr *strh = NULL, *dynstrh = NULL;

    for (int i = 0; i < ehdr.e_shnum; i++) {
        if (shdr[i].sh_type == SHT_SYMTAB) symh = &shdr[i];
        else if (shdr[i].sh_type == SHT_DYNSYM) dynsymh = &shdr[i];
        else if (shdr[i].sh_type == SHT_STRTAB) {
            if (strcmp(shstrtab + shdr[i].sh_name, ".strtab") == 0) strh = &shdr[i];
            else if (strcmp(shstrtab + shdr[i].sh_name, ".dynstr") == 0) dynstrh = &shdr[i];
        }
    }

    if (dynsymh && dynstrh) symtab->dyn = get_syms(fd, dynsymh, dynstrh);
    if (symh && strh) symtab->st = get_syms(fd, symh, strh);

    free(shstrtab);
    free(shdr);
    return 0;
}

static symtab_t load_symtab(const char *filename) {
    symtab_t symtab = (symtab_t) xmalloc(sizeof(*symtab));
    memset(symtab, 0, sizeof(*symtab));

    int fd = open(filename, O_RDONLY);
    if (fd < 0) {
        free(symtab);
        return NULL;
    }

    if (do_load(fd, symtab) < 0) {
        free(symtab);
        close(fd);
        return NULL;
    }

    close(fd);
    return symtab;
}

static int load_memmap(pid_t pid, struct mm *mm, int *nmmp) {
    char maps_path[256];
    char line[1024];
    FILE *maps;
    int nmm = 0;
    
    snprintf(maps_path, sizeof(maps_path), "/proc/%d/maps", pid);
    maps = fopen(maps_path, "r");
    if (!maps) {
        LOGE("Failed to open %s", maps_path);
        return -1;
    }

    while (fgets(line, sizeof(line), maps)) {
        char name[MAX_NAME_LEN] = {0};
        unsigned long start, end;
        
        int rv = sscanf(line, "%lx-%lx %*s %*s %*s %*s %s", &start, &end, name);
        
        if (rv == 2) {
            // Anonymous mapping
            mm[nmm].start = start;
            mm[nmm].end = end;
            strcpy(mm[nmm].name, MEMORY_ONLY);
            nmm++;
            continue;
        }
        
        if (rv == 3) {
            // Named mapping
            int i;
            for (i = nmm - 1; i >= 0; i--) {
                if (strcmp(mm[i].name, name) == 0) break;
            }
            
            if (i >= 0) {
                if (start < mm[i].start) mm[i].start = start;
                if (end > mm[i].end) mm[i].end = end;
            } else {
                mm[nmm].start = start;
                mm[nmm].end = end;
                strcpy(mm[nmm].name, name);
                nmm++;
            }
        }
    }

    fclose(maps);
    *nmmp = nmm;
    return 0;
}

static int find_libname(const char *libn, char *name, int len, unsigned long *start,
                       struct mm *mm, int nmm) {
    for (int i = 0; i < nmm; i++) {
        if (strcmp(mm[i].name, MEMORY_ONLY) == 0) continue;
        
        char *p = strrchr(mm[i].name, '/');
        if (!p) continue;
        p++;
        
        if (strncmp(libn, p, strlen(libn))) continue;
        p += strlen(libn);

        if (strncmp("so", p, 2) == 0 || true) {
            *start = mm[i].start;
            strncpy(name, mm[i].name, len - 1);
            name[len - 1] = '\0';
            
            mprotect((void *)mm[i].start, mm[i].end - mm[i].start,
                     PROT_READ | PROT_WRITE | PROT_EXEC);
            return 0;
        }
    }
    return -1;
}

static int lookup2(struct symlist *sl, unsigned char type, const char *name, unsigned long *val) {
    for (unsigned i = 0; i < sl->num; i++) {
        if (strcmp(sl->str + sl->sym[i].st_name, name) == 0 &&
            ELF32_ST_TYPE(sl->sym[i].st_info) == type) {
            *val = sl->sym[i].st_value;
            return 0;
        }
    }
    return -1;
}

static int lookup_sym(symtab_t s, unsigned char type, const char *name, unsigned long *val) {
    if (s->dyn && !lookup2(s->dyn, type, name, val)) return 0;
    if (s->st && !lookup2(s->st, type, name, val)) return 0;
    return -1;
}

static int lookup_func_sym(symtab_t s, const char *name, unsigned long *val) {
    return lookup_sym(s, STT_FUNC, name, val);
}

int find_name(pid_t pid, const char *name, const char *libn, unsigned long *addr) {
    struct mm mm[1000];
    unsigned long libcaddr;
    int nmm;
    char libc[1024];
    
    if (load_memmap(pid, mm, &nmm) < 0) {
        LOGE("Cannot read memory map");
        return -1;
    }
    
    if (find_libname(libn, libc, sizeof(libc), &libcaddr, mm, nmm) < 0) {
        LOGE("Cannot find library: %s", libn);
        return -1;
    }
    
    symtab_t s = load_symtab(libc);
    if (!s) {
        LOGE("Cannot read symbol table");
        return -1;
    }
    
    if (lookup_func_sym(s, name, addr) < 0) {
        LOGE("Cannot find function: %s", name);
        return -1;
    }
    
    *addr += libcaddr;
    return 0;
}

int find_libbase(pid_t pid, const char *libn, unsigned long *addr) {
    struct mm mm[1000];
    unsigned long libcaddr;
    int nmm;
    char libc[1024];
    
    if (load_memmap(pid, mm, &nmm) < 0) {
        LOGE("Cannot read memory map");
        return -1;
    }
    
    if (find_libname(libn, libc, sizeof(libc), &libcaddr, mm, nmm) < 0) {
        LOGE("Cannot find library");
        return -1;
    }
    
    *addr = libcaddr;
    return 0;
}