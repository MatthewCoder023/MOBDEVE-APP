#!/usr/bin/env python3
"""Fail if any 64-bit native library in an APK is not 16 KB aligned.

Android 15+ devices may use 16 KB memory pages. A shared library whose PT_LOAD
segments are aligned to the older 4 KB boundary cannot be mapped on those
devices, and Play rejects such uploads. Only 64-bit ABIs matter: 16 KB page
devices are 64-bit only, so armeabi-v7a/x86 alignment is not checked.

Usage: check_16kb_alignment.py <path-to.apk>
"""
import struct
import sys
import zipfile

REQUIRED_ALIGNMENT = 16 * 1024
SIXTY_FOUR_BIT_ABIS = {"arm64-v8a", "x86_64"}
PT_LOAD = 1


def load_segment_alignments(blob: bytes) -> list[int]:
    """p_align of every PT_LOAD segment in an ELF image."""
    if blob[:4] != b"\x7fELF":
        return []
    is_64_bit = blob[4] == 2
    endian = "<" if blob[5] == 1 else ">"

    if is_64_bit:
        ph_offset = struct.unpack_from(endian + "Q", blob, 0x20)[0]
        ph_entry_size = struct.unpack_from(endian + "H", blob, 0x36)[0]
        ph_count = struct.unpack_from(endian + "H", blob, 0x38)[0]
        align_offset, align_fmt = 48, endian + "Q"
    else:
        ph_offset = struct.unpack_from(endian + "I", blob, 0x1C)[0]
        ph_entry_size = struct.unpack_from(endian + "H", blob, 0x2A)[0]
        ph_count = struct.unpack_from(endian + "H", blob, 0x2C)[0]
        align_offset, align_fmt = 28, endian + "I"

    alignments = []
    for index in range(ph_count):
        entry = ph_offset + index * ph_entry_size
        if struct.unpack_from(endian + "I", blob, entry)[0] == PT_LOAD:
            alignments.append(struct.unpack_from(align_fmt, blob, entry + align_offset)[0])
    return alignments


def main(apk_path: str) -> int:
    offenders = []
    checked = 0
    with zipfile.ZipFile(apk_path) as apk:
        for name in apk.namelist():
            if not name.endswith(".so") or not name.startswith("lib/"):
                continue
            abi = name.split("/")[1]
            if abi not in SIXTY_FOUR_BIT_ABIS:
                continue
            alignments = load_segment_alignments(apk.read(name))
            if not alignments:
                continue
            checked += 1
            worst = min(alignments)
            if worst < REQUIRED_ALIGNMENT:
                offenders.append((name, worst))

    if offenders:
        print("16 KB alignment check FAILED")
        for name, align in offenders:
            print(f"  {name}: LOAD align {hex(align)} (needs >= {hex(REQUIRED_ALIGNMENT)})")
        print("\nUpgrade the dependency that ships these libraries.")
        return 1

    print(f"16 KB alignment OK ({checked} 64-bit native librar{'y' if checked == 1 else 'ies'} checked)")
    return 0


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print(__doc__)
        raise SystemExit(2)
    raise SystemExit(main(sys.argv[1]))
