curl -H "Content-Type: application/json" -X POST -d \
'{
  "auth_token":"AUTH_TOKEN"
}' \
https://chatapi.viber.com/pa/get_account_info | python -m json.tool
