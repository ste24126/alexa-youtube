import sys
from unittest.mock import MagicMock
import os

os.environ['DEVELOPER_KEY'] = 'test_key'
sys.modules['googleapiclient'] = MagicMock()
sys.modules['googleapiclient.discovery'] = MagicMock()
sys.modules['googleapiclient.errors'] = MagicMock()
sys.modules['pytube'] = MagicMock()
sys.modules['botocore'] = MagicMock()
sys.modules['botocore.vendored'] = MagicMock()
sys.modules['dateutil'] = MagicMock()
sys.modules['dateutil.tz'] = MagicMock()

import lambda_function

def test_build_speechlet_response():
    title = "Test Title"
    output = "Test Output"
    reprompt = "Test Reprompt"
    end_session = True

    response = lambda_function.build_speechlet_response(title, output, reprompt, end_session)

    assert response['outputSpeech']['type'] == 'PlainText'
    assert response['outputSpeech']['text'] == output
    assert response['card']['title'] == title
    assert response['card']['content'] == output
    assert response['reprompt']['outputSpeech']['text'] == reprompt
    assert response['shouldEndSession'] == end_session
    print("Test passed!")

if __name__ == '__main__':
    test_build_speechlet_response()
