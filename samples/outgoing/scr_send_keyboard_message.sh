curl -H "Content-Type: application/json" -X POST -d \
'{
  "auth_token": "AUTH_TOKEN",
  "receiver": "B97ZnbvEiCGiMVv6jFM0DA==",
  "type": "picture",
  "media": "http://www.miniapps.pro/wp-content/uploads/2015/02/variant2-2-1024x683.jpeg",
  "text": "with picture to all...",
  "keyboard": {
    "Type": "keyboard",
    "DefaultHeight": true,
    "BgColor": "#FFFFF0",
    "Buttons": [
      {
        "Columns": 6,
        "Rows": 2,
        "BgColor": "#2db9b9",
        "BgMediaType": "picture",
        "BgMedia": "http://eyeline.mobi/wp-content/uploads/2014/04/bg-office-01.jpg",
        "BgLoop": true,
        "ActionType": "open-url",
        "ActionBody": "http://www.miniapps.pro/",
        "Image": "http://www.miniapps.pro/wp-content/uploads/2016/08/logo-experiment.png",
        "Text": "<b>MINIAPPS.PRO</b>",
        "TextVAlign": "middle",
        "TextHAlign": "center",
        "TextOpacity": 60,
        "TextSize": "regular"
      },
      {
        "Columns": 6,
        "Rows": 2,
        "BgColor": "#2db9b9",
        "BgMediaType": "picture",
        "BgMedia": "http://www.miniapps.pro/wp-content/uploads/2015/02/variant2-2-1024x683.jpeg",
        "BgLoop": true,
        "ActionType": "open-url",
        "ActionBody": "http://www.eyeline.mobi/",
        "Image": "http://eyeline.mobi/wp-content/uploads/2016/02/logo.png",
        "Text": "<b>EYELINE.MOBI</b>",
        "TextVAlign": "middle",
        "TextHAlign": "center",
        "TextOpacity": 60,
        "TextSize": "regular"
      }
    ]
  }
}' \
https://chatapi.viber.com/pa/send_message | python -m json.tool
