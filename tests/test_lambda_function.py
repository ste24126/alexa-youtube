import unittest
from unittest.mock import patch

# Import the module to be tested
import lambda_function

class TestHandlePlayback(unittest.TestCase):
    def setUp(self):
        # Create a base event structure for testing
        self.base_event = {
            'request': {
                'type': ''
            }
        }

    @patch('lambda_function.started')
    def test_playback_started(self, mock_started):
        event = self.base_event.copy()
        event['request']['type'] = 'AudioPlayer.PlaybackStarted'
        mock_started.return_value = "started_result"

        result = lambda_function.handle_playback(event)

        mock_started.assert_called_once_with(event)
        self.assertEqual(result, "started_result")

    @patch('lambda_function.finished')
    def test_playback_finished(self, mock_finished):
        event = self.base_event.copy()
        event['request']['type'] = 'AudioPlayer.PlaybackFinished'
        mock_finished.return_value = "finished_result"

        result = lambda_function.handle_playback(event)

        mock_finished.assert_called_once_with(event)
        self.assertEqual(result, "finished_result")

    @patch('lambda_function.stopped')
    def test_playback_stopped(self, mock_stopped):
        event = self.base_event.copy()
        event['request']['type'] = 'AudioPlayer.PlaybackStopped'
        mock_stopped.return_value = "stopped_result"

        result = lambda_function.handle_playback(event)

        mock_stopped.assert_called_once_with(event)
        self.assertEqual(result, "stopped_result")

    @patch('lambda_function.nearly_finished')
    def test_playback_nearly_finished(self, mock_nearly_finished):
        event = self.base_event.copy()
        event['request']['type'] = 'AudioPlayer.PlaybackNearlyFinished'
        mock_nearly_finished.return_value = "nearly_finished_result"

        result = lambda_function.handle_playback(event)

        mock_nearly_finished.assert_called_once_with(event)
        self.assertEqual(result, "nearly_finished_result")

    @patch('lambda_function.failed')
    def test_playback_failed(self, mock_failed):
        event = self.base_event.copy()
        event['request']['type'] = 'AudioPlayer.PlaybackFailed'
        mock_failed.return_value = "failed_result"

        result = lambda_function.handle_playback(event)

        mock_failed.assert_called_once_with(event)
        self.assertEqual(result, "failed_result")

    @patch('lambda_function.started')
    @patch('lambda_function.finished')
    @patch('lambda_function.stopped')
    @patch('lambda_function.nearly_finished')
    @patch('lambda_function.failed')
    def test_unknown_playback_event(self, mock_failed, mock_nearly_finished, mock_stopped, mock_finished, mock_started):
        event = self.base_event.copy()
        event['request']['type'] = 'AudioPlayer.UnknownEvent'

        result = lambda_function.handle_playback(event)

        self.assertIsNone(result)
        mock_started.assert_not_called()
        mock_finished.assert_not_called()
        mock_stopped.assert_not_called()
        mock_nearly_finished.assert_not_called()
        mock_failed.assert_not_called()

if __name__ == '__main__':
    unittest.main()
