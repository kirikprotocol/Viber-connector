curl -H "Content-Type: application/json" -X POST -d \
'{
  "auth_token":"AUTH_TOKEN",
  "receiver":"B97ZnbvEiCGiMVv6jFM0DA==",
  "sender":
  {
    "name": "MINIAPPS BONUS PACK",
    "avatar": "http://www.miniapps.pro/wp-content/uploads/2015/02/graphic.png"
  },
  "tracking_data": "tracking data what is this",
  "type": "url",
  "media": "http://eyeline.ru/"
}' \
https://chatapi.viber.com/pa/send_message | python -m json.tool
