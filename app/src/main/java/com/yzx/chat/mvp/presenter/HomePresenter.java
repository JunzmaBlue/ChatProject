package com.yzx.chat.mvp.presenter;

import android.os.Handler;

import com.yzx.chat.bean.ContactBean;
import com.yzx.chat.bean.ContactOperationBean;
import com.yzx.chat.bean.GroupBean;
import com.yzx.chat.mvp.contract.HomeContract;
import com.yzx.chat.mvp.view.activity.ChatActivity;
import com.yzx.chat.mvp.view.activity.HomeActivity;
import com.yzx.chat.mvp.view.activity.NotificationMessageActivity;
import com.yzx.chat.network.chat.ChatManager;
import com.yzx.chat.network.chat.ContactManager;
import com.yzx.chat.network.chat.ConversationManager;
import com.yzx.chat.network.chat.IMClient;
import com.yzx.chat.tool.NotificationHelper;
import com.yzx.chat.util.AndroidUtil;

import io.rong.imlib.model.Message;

/**
 * Created by YZX on 2017年11月15日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */

public class HomePresenter implements HomeContract.Presenter {

    private HomeContract.View mHomeView;
    private Handler mHandler;
    private IMClient mIMClient;

    @Override
    public void attachView(HomeContract.View view) {
        mHomeView = view;
        mHandler = new Handler();
        mIMClient = IMClient.getInstance();
        mIMClient.getConversationManager().addConversationUnreadCountListener(mOnConversationUnreadCountListener);
        mIMClient.getChatManager().addOnMessageReceiveListener(mOnChatMessageReceiveListener, null);
        mIMClient.getContactManager().addContactOperationUnreadCountChangeListener(mOnContactOperationUnreadCountChangeListener);
        mIMClient.getContactManager().addContactOperationListener(mOnContactOperationListener);
    }

    @Override
    public void detachView() {
        mIMClient.getConversationManager().removeConversationUnreadCountListener(mOnConversationUnreadCountListener);
        mIMClient.getChatManager().removeOnMessageReceiveListener(mOnChatMessageReceiveListener);
        mIMClient.getContactManager().removeContactOperationUnreadCountChangeListener(mOnContactOperationUnreadCountChangeListener);
        mIMClient.getContactManager().removeContactOperationListener(mOnContactOperationListener);
        mHandler.removeCallbacksAndMessages(null);
        mHomeView = null;
        mIMClient = null;
        mHandler = null;
    }


    @Override
    public void loadUnreadCount() {
        mHomeView.updateMessageUnreadBadge(mIMClient.getConversationManager().getConversationUnreadCount());
        mHomeView.updateContactUnreadBadge(mIMClient.getContactManager().getContactUnreadCount());
        mIMClient.getConversationManager().updateChatUnreadCount();
        mIMClient.getContactManager().updateContactUnreadCount();
    }

    private final ConversationManager.OnConversationUnreadCountListener mOnConversationUnreadCountListener = new ConversationManager.OnConversationUnreadCountListener() {
        @Override
        public void OnConversationUnreadCountChange(final int count) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mHomeView.updateMessageUnreadBadge(count);
                }
            });
        }
    };

    private final ContactManager.OnContactOperationUnreadCountChangeListener mOnContactOperationUnreadCountChangeListener = new ContactManager.OnContactOperationUnreadCountChangeListener() {
        @Override
        public void onContactOperationUnreadCountChange(final int count) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mHomeView.updateContactUnreadBadge(count);
                }
            });
        }
    };

    private final ChatManager.OnChatMessageReceiveListener mOnChatMessageReceiveListener = new ChatManager.OnChatMessageReceiveListener() {
        @Override
        public void onChatMessageReceived(final Message message, int untreatedCount) {
            Class activityClass = AndroidUtil.getStackTopActivityClass();
            if (activityClass == HomeActivity.class || (activityClass == ChatActivity.class && message.getTargetId().equals(ChatPresenter.sConversationID))) {
                return;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    switch (message.getConversationType()) {
                        case PRIVATE:
                            ContactBean contact = IMClient.getInstance().getContactManager().getContact(message.getTargetId());
                            if (contact != null) {
                                NotificationHelper.getInstance().showPrivateMessageNotification(message, contact);
                            }
                            break;
                        case GROUP:
                            GroupBean group = IMClient.getInstance().getGroupManager().getGroup(message.getTargetId());
                            if (group != null) {
                                NotificationHelper.getInstance().showGroupMessageNotification(message, group);
                            }
                            break;
                    }
                }
            });
        }
    };

    private final ContactManager.OnContactOperationListener mOnContactOperationListener = new ContactManager.OnContactOperationListener() {
        @Override
        public void onContactOperationReceive(final ContactOperationBean contactOperation) {
            Class activityClass = AndroidUtil.getStackTopActivityClass();
            if (activityClass == HomeActivity.class || activityClass == NotificationMessageActivity.class) {
                return;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    NotificationHelper.getInstance().showContactOperationNotification(contactOperation);
                }
            });
        }

        @Override
        public void onContactOperationUpdate(ContactOperationBean contactOperation) {

        }

        @Override
        public void onContactOperationRemove(final ContactOperationBean contactOperation) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    NotificationHelper.getInstance().cancelNotification(contactOperation.getUserID().hashCode());
                }
            });
        }
    };

}
