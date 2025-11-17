set -euo pipefail

html="$1"

score=$(
  awk '
    /Mutation Coverage/ { flag=1; next }
    flag {
      for (i = 1; i <= NF; i++) {
        if ($i ~ /%$/) {
          pctCount++
          if (pctCount == 2) {
            val = $i
            gsub(/[^0-9]/, "", val)
            print val
            exit
          }
        }
      }
    }
  ' "$html"
)

if [ -z "$score" ]; then
  echo "Impossible de lire le score de mutation dans $html" >&2
  exit 1
fi

echo "$score"
