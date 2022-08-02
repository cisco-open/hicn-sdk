#!/bin/bash
set -e
bin=$1
echo change_path "$bin"
dyl_list=(`otool -L "$bin" | awk '{$1=$1;print}' | grep -v "^/usr/lib/" | grep -v ":" | awk '{print $1}'`)

for dyl in ${dyl_list[*]}; do
	libnamedyl=$(basename $dyl)
        install_name_tool -change "$dyl" @rpath/"$libnamedyl" "$bin"
done
install_name_tool -id @rpath/"$(basename $bin)" "$bin"
