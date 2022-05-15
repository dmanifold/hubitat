#!/usr/bin/env bash
urlencode() {
    # urlencode <string>

    old_lc_collate=$LC_COLLATE
    LC_COLLATE=C

    local length="${#1}"
    for (( i = 0; i < length; i++ )); do
        local c="${1:$i:1}"
        case $c in
            [a-zA-Z0-9.~_-]) printf '%s' "$c" ;;
            *) printf '%%%02X' "'$c" ;;
        esac
    done

    LC_COLLATE=$old_lc_collate
}
getbatteryInfo() {
  echo "$(pmset -g batt)"
}
sendInfo() {
MACINFO="$(urlencode "${1}")"
curl -s "http://${HUB_IP}/apps/api/${APP_ID}/devices/${DEVICE_ID}/setInfo/${MACINFO}?access_token=${ACCESS_TOKEN}" > /dev/null
}
createEnv() {
  ENV_FILE="${1}"
  echo 'ACCESS_TOKEN=""' > "${ENV_FILE}"
  echo 'APP_ID=""' >> "${ENV_FILE}"
  echo 'DEVICE_ID=""' >> "${ENV_FILE}"
  echo 'HUB_IP=""' >> "${ENV_FILE}"
  echo "Add your hub & device values to ${ENV_FILE}"
  echo "add the following to your crontab"
  echo "*/5 * * * * $(readlink -f ${0})"
}

ENV_FILE="$(readlink -f $(dirname ${0}))/.env"
if [[ -f "${ENV_FILE}" ]]; then
    source "${ENV_FILE}"
    sendInfo "$(getbatteryInfo)"
else
  createEnv "${ENV_FILE}"
fi

