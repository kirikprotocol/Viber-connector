curl -H "Content-Type: application/json" -X POST -d \
'{
  "auth_token":"AUTH_TOKEN",
  "receiver":"B97ZnbvEiCGiMVv6jFM0DA==",
  "type":"text",
  "text":"viam **supervadet**"
}' \
https://chatapi.viber.com/pa/send_message | python -m json.tool
