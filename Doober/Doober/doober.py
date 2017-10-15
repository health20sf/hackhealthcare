from flask import Flask
from slackclient import client as SlackClient
import bot_details
import slack_commands
app = Flask(__name__)

SLACK_BOT_TOKEN= bot_details.Bot_cred['SLACK_BOT_TOKEN']
BOT_NAME= bot_details.Bot_cred['BOT_NAME']


def get_bot_id():
    bot_id = ""
    slack_client = slack_commands.get_slack_handle()
    if __name__ == "__main__":
        api_call = slack_client.api_call("users.list")
        if api_call.get('ok'):
            # retrieve all users so we can find our bot
            users = api_call.get('members')
            for user in users:
                if user['name'] == BOT_NAME:
                    bot_id = user['id']
        else:
            bot_id = ''
    return bot_id

@app.route('/GetBotName')
def get_bot_name():
    return 'BOT_NAME'

@app.route('/')
def base_url():
    return get_bot_id()

if __name__ == '__main__':
    app.run()