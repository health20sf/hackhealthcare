import slackclient.client as Slack_Client
import bot_details
import time

slack_handle = None

EXAMPLE_COMMAND = "do"

def initialize_slack_client():
    global slack_handle
    slack_handle = Slack_Client.SlackClient(bot_details.Bot_cred['SLACK_BOT_TOKEN'])


def get_slack_handle():
    if slack_handle == None:
        initialize_slack_client()
    return slack_handle

def format_at_command(verb):
    return "<@" + verb + ">"

def handle_command(command, channel,verb):
    """
        Receives commands directed at the bot and determines if they
        are valid commands. If so, then acts on the commands. If not,
        returns back what it needs for clarification.
    """
    slack_client = get_slack_handle()
    response = "Not sure what you mean. Use the *" + verb + \
               "* command with numbers, delimited by spaces."
    if command.startswith(verb):
        response = "Sure...write some more code then I can do that!"
    slack_handle.api_call("chat.postMessage", channel=channel,
                          text=response, as_user=True)


def parse_slack_output(slack_rtm_output):
    """
        The Slack Real Time Messaging API is an events firehose.
        this parsing function returns None unless a message is
        directed at the Bot, based on its ID.
    """
    output_list = slack_rtm_output
    if output_list and len(output_list) > 0:
        for output in output_list:
            if output and 'text' in output and format_at_command(bot_details.Bot_cred['BOT_ID']) in output['text']:
                # return text after the @ mention, whitespace removed
                return output['text'].split(AT_BOT)[1].strip().lower(), \
                       output['channel']
    return None, None


if __name__ == "__main__":
    READ_WEBSOCKET_DELAY = 1 # 1 second delay between reading from firehose
    slack_client = get_slack_handle()
    if slack_client.rtm_connect():
        print("StarterBot connected and running!")
        while True:
            command, channel = parse_slack_output(slack_client.rtm_read())
            if command and channel:
                handle_command(command, channel)
            time.sleep(READ_WEBSOCKET_DELAY)
    else:
        print("Connection failed. Invalid Slack token or bot ID?")