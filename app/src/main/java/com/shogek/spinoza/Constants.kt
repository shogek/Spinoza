package com.shogek.spinoza

object Extra {
    const val GOAL = "GOAL"

    object MessageList {
        object ContactListForward {
            object ForwardMessage {
                const val MESSAGE = "MessageList - ContactListForward - ForwardMessage - MESSAGE"
                const val CONTACT_ID = "MessageList - ContactListForward - ForwardMessage - CONTACT_ID"
            }
        }
    }

    object ConversationList {
        object MessageList {
            object OpenConversation {
                const val GOAL = "ConversationList - MessageList - OpenConversation - GOAL"
                const val CONVERSATION_ID = "ConversationList - MessageList - OpenConversation - CONVERSATION_ID"
            }
            object NewMessage {
                const val GOAL = "ConversationList - MessageList - NewMessage - INTENTION"
                const val CONVERSATION_ID = "ConversationList - MessageList - NewMessage - CONVERSATION_ID"
                const val CONTACT_ID = "ConversationList - MessageList - NewMessage - CONTACT_ID"
            }

        }
    }

    object ContactList {
        object ConversationList {
            object PickContact {
                const val CONTACT_ID = "ContactList - ConversationList - PickContact - CONTACT_ID"
            }
        }
    }

    object MessageNotification {
        object MessageList {
            object MessageReceived {
                const val GOAL = "MessageNotification - MessageList - MessageReceived - INTENTION"
                const val CONVERSATION_ID = "MessageNotification - MessageList - MessageReceived - CONVERSATION_ID"
            }
        }
    }
}
