#!/bin/bash


lang_map=$(curl -s "https://www.localeplanet.com/api/en/langmap.json")
IFS=',' read -ra lang_lines <<< "$(echo "$lang_map" | tr -d '{}" ')"

langs=()

for ll in "${lang_lines[@]}"; do
    IFS=':' read -ra ll_array <<< "$ll"
    lang="{name: \"${ll_array[1]}\", alpha2: \"${ll_array[0]}\"}"
    echo "parsed $lang"
    langs+=("$lang")
done

filename="localeplanet-$(date +"%Y%m%d").json"
IFS=',' json_output="${langs[*]}"
echo "[$json_output]" > "$filename"

echo "downloaded locale infos to $filename"