#ifndef __SUBSTRATEHOOK_H__
#define __SUBSTRATEHOOK_H__


#include <stdlib.h>

#define _extern extern "C" __attribute__((__visibility__("hidden")))

#ifdef __cplusplus
extern "C" {
#endif

void MSHookFunction(void *symbol, void *replace, void **result);
void MSHookMemory(void *target, const void *data, size_t size);
void SubstrateMemoryUnprotect(void *address, size_t size);

#ifdef __cplusplus
}
#endif

#endif
