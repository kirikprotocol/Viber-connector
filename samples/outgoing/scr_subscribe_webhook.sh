curl -H "Content-Type: application/json" -X POST -d \
'{
  "auth_token":"AUTH_TOKEN",
  "url":"https://devel.globalussd.mobi/dbot/viber/logic.jsp",
  "event_types":["message","delivered","seen"]
}' \
https://chatapi.viber.com/pa/set_webhook

# Thu Nov 17 15:42:27 +07 2016 Received: {"status":0,"status_message":"ok"}
# And webhook calback: {"event":"webhook","timestamp":4979957288990561233,"message_token":MESSAGE_TOKEN}
