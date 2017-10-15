# Selena Health

## Deploy to Heroku

Requirements: git, [heroku cli](https://heroku.com).

From the terminal on your computer, clone this git distribution.
Set the optional Node environment vars:

    heroku config:set NODEJS_VERSION=7.4.0
    heroku config:set NODE_TLS_REJECT_UNAUTHORIZED=0

 Build a new app from the distribution:

    heroku apps:create
    git push heroku master

Verify by opening the app homepage in a browser:

    heroku open

Note the URL for later use in the configuration.

## Facebook Messenger Setup

- Create a page on Facebook for your service.

### Configuring Messenger

From your [Facebook developer account](https://developers.facebook.com):

- Create a new facebook app and open its configuration page, then:

#### Under *Products*

- add **Messenger** as product.

#### Under *Token Generation*

- select the created facebook page.
- Add the new access token as an environment var for the server:

    heroku config:set FACEBOOK_ACCESS_TOKEN=xxxxxx

#### Under *Webhooks*

- First make sure your app is installed on heroku and up running, as the webhook on the server will be accessed by facebook to verify it.
- Configure the facebook webhook *Callback Url* to point to `https://appname.herokuapp.com/fbme/webhook` where `appname` is the name of your app on heroku.
- If the webhook fails to be accepted by facebook, troubleshoot as needed.
- Set the facebook verify token to a complex string of your choice.
- Set the facebook verify token var on the server to a string of your choice by executing in the terminal:

    heroku config:set FACEBOOK_VERIFY_TOKEN="some secret text"

- edit the subscription fields to enable "messages" only.
- Verify and save the Webhooks dialog.
- Select your page as subscriber to webhook events.

#### Under *Built-in NLP*

- Enable *Built-in NLP* which provides language analysis from [wit.ai](https://wit.ai)

#### Under *App Review*

- Select `pages_messaging` and `pages_messaging_subscriptions` then submit for review (optional for development but required to make the service public).
- Note: You may have to update the facebook page as requested.

### Configuring the Messenger Webhook

- Select *WebHooks* on the drawer or:
- Add a [webhook](https://developers.facebook.com/docs/messenger-platform/webhook-reference) "product" to handle messages for the app.
- Set the type of the webhook to `page` as needed.

### Configuring the Chat Extension

The Facebook chat extension provides a menu and view within Messenger. CoCoCare uses this to provide an interactive dialog in Messenger for patients to check-in with their symptoms.

Start a repl on heroku to execute configuration commands:

    heroku run lein repl

- Evaluate to provide configuration commands:

    (in-ns 'sdk.facebook.messenger)

- Whitelist the domain of the server:

    (send-whitelist-domains ["https://appname.herokuapp.com/"])

- To create a messenger extension link in the facebook messenger drawer, using the name of your own app, evaluate:

    (send-home-url {:url "https://appname.herokuapp.com/#checkin"
                    :webview_height_ratio "tall"
                    :in_test "true"})

- Enable a Getting Started button in Messenger:

    (send-get-started {:payload "START"})

- Set up a menu in Messenger:

    (send-persistent-menu
         [{:locale "default"
           :call_to_actions
           [(url-button "Schedule Appointment"
                        :messenger-extensions true
                        :url "https://appname.herokuapp.com/#checkin"
                        :webview-height-ratio "compact")]}])

## Run Locally

Requirements: leiningen, node, npm

To start a server on your own computer:

    lein do clean, deps, compile
    lein run

Point your browser to the displayed local port.

## Development Workflow

Start figwheel for interactive development with
automatic builds and code loading:

    lein figwheel app server

Wait until Figwheel is ready to connect, then
start a server in another terminal:

    lein run

Open the displayed URL in a browser.
Figwheel will push code changes to the app and server.

To test the system, execute:

    lein test

## Local Testing

For development purposes, a staging server on Heroku can optionally forward
incoming webhooks to ngrok. That way you can test on your local machine without having to reconfigure the webhooks on facebook and cisco spark.

In the project directory execute in terminal to set up the local environment:

    touch .env
    heroku config >> .env

For testing, start e.g. ngrok for local dev:

    ngrok http 5000

Set the heroku system var REDIRECT to the url provided when running ngrok on your
local computer, using a command like this in the Terminal:

    heroku config:set REDIRECT=https://f1f362f1.ngrok.io

Start server locally:

    heroku local web

Afterwards, disable the redirect:

    heroku config:remove REDIRECT

## License

Copyright © 2017 Terje Norderhaug

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
