package com.glasswork.dettbox;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.glasswork.dettbox.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MyService extends Service {
    public Calendar endDate;
    public Calendar start;
    public static final String FIREBASE_LINK = "https://dettbox-default-rtdb.europe-west1.firebasedatabase.app";
    public int totalSeconds;
    public User user;
    // new way for package info
    private UsageStatsManager mUsageStatsManager;
    private PackageManager mPm;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onCreate() {
        super.onCreate();

        if (checkUsageStatsPermision()) {

            start = Calendar.getInstance();
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.HOUR_OF_DAY, 0);

            endDate = Calendar.getInstance();
            endDate.set(Calendar.SECOND, 59);
            endDate.set(Calendar.MINUTE, 59);
            endDate.set(Calendar.HOUR_OF_DAY, 23);

        } else {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }



    }

    private String convertTimeToString(long lastTimeUsed) {
        return String.format(
                "%02dh %02dm %02ds",
                TimeUnit.MILLISECONDS.toHours(lastTimeUsed),
                TimeUnit.MILLISECONDS.toMinutes(lastTimeUsed) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(lastTimeUsed)
                ),
                TimeUnit.MILLISECONDS.toSeconds(lastTimeUsed) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(lastTimeUsed)
                )
        );
    }

    private boolean checkUsageStatsPermision() {
        try {
            AppOpsManager appOpsManager;
            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(getPackageName(), 0);
            int mode = 0;
            appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return mode == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error: Cannot find any usage stats", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /*// shows a notification when the app is opened and keeps it until app is force closed
    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);


    }*/


    /*@RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        //startTimer();

        return START_STICKY;
    }*/


    /*@RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onDestroy() {
        super.onDestroy();
        stoptimertask();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
    }*/


    /*private Timer timer;
    private TimerTask timerTask;
    public void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            public void run() {
                *//*Log.i("Count", "=========  "+ (counter++));*//*
                //showUsageStats();
                //try {

                    Calendar currentDate = Calendar.getInstance();

                    *//*if (currentDate.before(endDate)) {
                        *//**//*Toast.makeText(getApplicationContext(), "DAY NOT ENDED:", Toast.LENGTH_SHORT).show();*//**//*
                        *//**//*Log.e("TAG", "DAY NOT ENDED: ");*//**//*

                    } else {
                        *//**//*Toast.makeText(getApplicationContext(), "S'està reproduint (" + mQuery + ")", Toast.LENGTH_SHORT).show();*//**//*
                        *//**//*Log.e("TAG", "DAY ENDED: ");*//**//*
                        endDate = Calendar.getInstance();
                        endDate.set(Calendar.SECOND, 59);
                        endDate.set(Calendar.MINUTE, 59);
                        endDate.set(Calendar.HOUR_OF_DAY, 23);
                        start = Calendar.getInstance();
                        start.set(Calendar.SECOND, 0);
                        start.set(Calendar.MINUTE, 0);
                        start.set(Calendar.HOUR_OF_DAY, 0);
                    }*//*
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
                    cal.clear(Calendar.MINUTE);
                    cal.clear(Calendar.SECOND);
                    cal.clear(Calendar.MILLISECOND);

                    cal.set(Calendar.DAY_OF_MONTH, 1);

                    *//*getAppData(MyService.this, "com.whatsapp", cal.getTimeInMillis(), System.currentTimeMillis());
                    getAppData(MyService.this, "com.tinder", cal.getTimeInMillis(), System.currentTimeMillis());
                    getAppData(MyService.this, "com.glasswork.dettbox", cal.getTimeInMillis(), System.currentTimeMillis());*//*

                    *//*getTimeSpent(MyService.this, "com.whatsapp", cal.getTimeInMillis(), System.currentTimeMillis());
                    getTimeSpent(MyService.this, "deezer.android.app", cal.getTimeInMillis(), System.currentTimeMillis());
                    getTimeSpent(MyService.this, "com.glasswork.dettbox", cal.getTimeInMillis(), System.currentTimeMillis());
                    getTimeSpent(MyService.this, "com.instagram.android", cal.getTimeInMillis(), System.currentTimeMillis());
                    getTimeSpent(MyService.this, "com.netflix.mediaclient", cal.getTimeInMillis(), System.currentTimeMillis());
                    getTimeSpent(MyService.this, "org.telegram.messenger", cal.getTimeInMillis(), System.currentTimeMillis());
                    getTimeSpent(MyService.this, "com.discord", cal.getTimeInMillis(), System.currentTimeMillis());
                    getTimeSpent(MyService.this, "com.twitter.android", cal.getTimeInMillis(), System.currentTimeMillis());
                    getTimeSpent(MyService.this, "tv.twitch.android.app", cal.getTimeInMillis(), System.currentTimeMillis());
                    getTimeSpent(MyService.this, "com.google.android.youtube", cal.getTimeInMillis(), System.currentTimeMillis());
                    getTimeSpent(MyService.this, "com.zhiliaoapp.musically", cal.getTimeInMillis(), System.currentTimeMillis());
                    getTimeSpent(MyService.this, "com.snapchat.android", cal.getTimeInMillis(), System.currentTimeMillis());
                    getTimeSpent(MyService.this, "com.tinder", cal.getTimeInMillis(), System.currentTimeMillis());
                    getTimeSpent(MyService.this, "com.facebook.katana", cal.getTimeInMillis(), System.currentTimeMillis());
                    getTimeSpent(MyService.this, "com.facebook.orca", cal.getTimeInMillis(), System.currentTimeMillis());
                    getTimeSpent(MyService.this, "com.reddit.frontpage", cal.getTimeInMillis(), System.currentTimeMillis());
                    getTimeSpent(MyService.this, "com.bereal.ft", cal.getTimeInMillis(), System.currentTimeMillis());
                    getTimeSpent(MyService.this, "com.vanced.android.youtube", cal.getTimeInMillis(), System.currentTimeMillis());*//*

                    //saveTotalTimeToFirebase();

                *//*} catch (ParseException e) {
                    e.printStackTrace();
                }*//*
            }
        };
        timer.schedule(timerTask, 1000, 1000); //
    }*/

    public void getAppData(Context context, String packageName, long startSeason, long actualTime) {

        /*Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        cal.set(Calendar.DAY_OF_MONTH, 1);*/

        mUsageStatsManager = (UsageStatsManager)getSystemService(context.USAGE_STATS_SERVICE);
        final List<UsageStats> stats =
                mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,
                        startSeason, actualTime);
        if (stats == null) {
            return;
        }

        ArrayMap<String, UsageStats> map = new ArrayMap<>();
        final int statCount = stats.size();
        for (int i = 0; i < statCount; i++) {

            final android.app.usage.UsageStats pkgStats = stats.get(i);

            if (pkgStats.getPackageName().equals(packageName)) {
                // load application labels for each application
                Log.w("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "PACKAGENAME: " + pkgStats.getPackageName() + " Time: " + pkgStats.getTotalTimeInForeground());


                    String appName = getAppName(packageName);
                    /*UsageStats existingStats =
                            map.get(pkgStats.getPackageName());
                    if (existingStats == null) {
                        map.put(pkgStats.getPackageName(), pkgStats);
                    } else {
                        existingStats.add(pkgStats);
                    }*/

                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        FirebaseDatabase.getInstance(FIREBASE_LINK)
                                .getReference("Users")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child("Apps")
                                .child(packageName.replace(".", "-"))
                                .child("time")
                                .setValue(convertTimeToString(pkgStats.getTotalTimeInForeground()*1000));

                        /*String appName;
                        switch (packageName) {
                            case "com.whatsapp":
                                appName = "WhatsApp";
                                break;
                            case "deezer.android.app":
                                appName = "Deezer";
                                break;
                            case "com.glasswork.dettbox":
                                appName = "Dettbox";
                                break;
                            case "com.instagram.android":
                                appName = "Instagram";
                                break;
                            case "com.netflix.mediaclient":
                                appName = "Netflix";
                                break;
                            case "org.telegram.messenger":
                                appName = "Telegram";
                                break;
                            case "com.discord":
                                appName = "Discord";
                                break;
                            case "com.twitter.android":
                                appName = "Twitter";
                                break;
                            case "tv.twitch.android.app":
                                appName = "Twitch";
                                break;
                            case "com.google.android.youtube":
                                appName = "YouTube";
                                break;
                            case "com.zhiliaoapp.musically":
                                appName = "TikTok";
                                break;
                            case "com.snapchat.android":
                                appName = "Snapchat";
                                break;
                            case "com.tinder":
                                appName = "Tinder";
                                break;
                            case "com.facebook.katana":
                                appName = "Facebook";
                                break;
                            case "com.facebook.orca":
                                appName = "Messenger";
                                break;
                            case "com.reddit.frontpage":
                                appName = "Reddit";
                                break;
                            case "com.bereal.ft":
                                appName = "BeReal";
                                break;
                case "com.vanced.android.youtube":
                    appName = "YouTube Vanced";
                    break;
                            default:
                                appName = "App not found";
                        }*/

                        FirebaseDatabase.getInstance(FIREBASE_LINK)
                                .getReference("Users")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child("Apps")
                                .child(packageName.replace(".", "-"))
                                .child("name")
                                .setValue(appName);
                    }


            }


        }

    }

    private String getAppName(String packageName) {
        String appName;
        switch (packageName) {
            case "com.whatsapp":
                appName = "WhatsApp";
                break;
            case "deezer.android.app":
                appName = "Deezer";
                break;
            case "com.glasswork.dettbox":
                appName = "Dettbox";
                break;
            case "com.instagram.android":
                appName = "Instagram";
                break;
            case "com.netflix.mediaclient":
                appName = "Netflix";
                break;
            case "org.telegram.messenger":
                appName = "Telegram";
                break;
            case "com.discord":
                appName = "Discord";
                break;
            case "com.twitter.android":
                appName = "Twitter";
                break;
            case "tv.twitch.android.app":
                appName = "Twitch";
                break;
            case "com.google.android.youtube":
                appName = "YouTube";
                break;
            case "com.zhiliaoapp.musically":
                appName = "TikTok";
                break;
            case "com.snapchat.android":
                appName = "Snapchat";
                break;
            case "com.tinder":
                appName = "Tinder";
                break;
            case "com.facebook.katana":
                appName = "Facebook";
                break;
            case "com.facebook.orca":
                appName = "Messenger";
                break;
            case "com.reddit.frontpage":
                appName = "Reddit";
                break;
            case "com.bereal.ft":
                appName = "BeReal";
                break;
                /*case "com.vanced.android.youtube":
                    appName = "YouTube Vanced";
                    break;*/
            default:
                appName = "App not found";
        }
        return appName;
    }

    public int convertTimeToSeconds(String timeString) {
        String[] aux = timeString.split("h ", 2);
        String hours = aux[0];
        String[] aux2 = aux[1].split("m ", 2);
        String mins = aux2[0];
        String[] aux3 = aux2[1].split("s", 2);
        String sec = aux3[0];
        return Integer.parseInt(hours)*60*60 + Integer.parseInt(mins)*60 + Integer.parseInt(sec);
    }

    void saveTotalTimeToFirebase () {
        //readUser();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance(FIREBASE_LINK)
                    .getReference("Users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("Apps");

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int totalTime = 0;
                        for (DataSnapshot dataSnapshot:snapshot.getChildren()) {
                            String time = dataSnapshot.child("time").getValue().toString();

                            totalTime += convertTimeToSeconds(time);

                        }
                        // boolean if player is in the group or not to save its info
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        Boolean yourLocked = prefs.getBoolean(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupStatus", true);
                        String prefsGroupName = prefs.getString(FirebaseAuth.getInstance().getCurrentUser().getUid() + "groupName", "null");
                        if (!yourLocked) {
                            if (!prefsGroupName.equals("null")) {
                                String timeString = convertTimeToString(totalTime*1000L);
                                FirebaseDatabase.getInstance(FIREBASE_LINK)
                                        .getReference("Groups")
                                        .child(prefsGroupName)
                                        .child("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child("totalMinutes")
                                        .setValue(timeString);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    void getTimeSpent(Context context, String packageName, long beginTime, long endTime) throws ParseException {
        UsageEvents.Event currentEvent;
        List<UsageEvents.Event> allEvents = new ArrayList<>();
        HashMap<String, Integer> appUsageMap = new HashMap<>();

        UsageStatsManager usageStatsManager = (UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE);
        UsageEvents usageEvents = usageStatsManager.queryEvents(beginTime, endTime);

        while (usageEvents.hasNextEvent()) {
            currentEvent = new UsageEvents.Event();
            usageEvents.getNextEvent(currentEvent);
            if(currentEvent.getPackageName().equals(packageName) || packageName == null) {
                if (currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED
                        || currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED) {
                    allEvents.add(currentEvent);
                    String key = currentEvent.getPackageName();

                    Calendar currentDate = Calendar.getInstance();

                    if (currentDate.before(endDate)) {
                        /*Toast.makeText(getApplicationContext(), "DAY NOT ENDED:", Toast.LENGTH_SHORT).show();*/
                        /*Log.e("TAG", "DAY NOT ENDED: ");*/

                    } else {
                        /*Toast.makeText(getApplicationContext(), "DAY ENDED:", Toast.LENGTH_SHORT).show();*/
                        /*Log.e("TAG", "DAY ENDED: ");*/
                        endDate = Calendar.getInstance();
                        endDate.set(Calendar.SECOND, 59);
                        endDate.set(Calendar.MINUTE, 59);
                        endDate.set(Calendar.HOUR_OF_DAY, 23);
                        appUsageMap.put(key, 0);
                    }
                }
            }
        }

        for (int i = 0; i < allEvents.size() - 1; i++) {
            UsageEvents.Event E0 = allEvents.get(i);
            UsageEvents.Event E1 = allEvents.get(i + 1);

            if (E0.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED
                    && E1.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED
                    && E0.getClassName().equals(E1.getClassName())) {
                int diff = (int)(E1.getTimeStamp() - E0.getTimeStamp());
                diff /= 1000;
                Integer prev = appUsageMap.get(E0.getPackageName());
                if(prev == null) prev = 0;
                appUsageMap.put(E0.getPackageName(), prev + diff);
            }
        }

        int seconds;

        if (appUsageMap.get(packageName) != null) {
            seconds = appUsageMap.get(packageName);
        } else {
            seconds = 0;
        }
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseDatabase.getInstance(FIREBASE_LINK)
                    .getReference("Users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("Apps")
                    .child(packageName.replace(".", "-"))
                    .child("time")
                    .setValue(convertTimeToString(seconds*1000));

            String appName;
            switch (packageName) {
                case "com.whatsapp":
                    appName = "WhatsApp";
                    break;
                case "deezer.android.app":
                    appName = "Deezer";
                    break;
                case "com.glasswork.dettbox":
                    appName = "Dettbox";
                    break;
                case "com.instagram.android":
                    appName = "Instagram";
                    break;
                case "com.netflix.mediaclient":
                    appName = "Netflix";
                    break;
                case "org.telegram.messenger":
                    appName = "Telegram";
                    break;
                case "com.discord":
                    appName = "Discord";
                    break;
                case "com.twitter.android":
                    appName = "Twitter";
                    break;
                case "tv.twitch.android.app":
                    appName = "Twitch";
                    break;
                case "com.google.android.youtube":
                    appName = "YouTube";
                    break;
                case "com.zhiliaoapp.musically":
                    appName = "TikTok";
                    break;
                case "com.snapchat.android":
                    appName = "Snapchat";
                    break;
                case "com.tinder":
                    appName = "Tinder";
                    break;
                case "com.facebook.katana":
                    appName = "Facebook";
                    break;
                case "com.facebook.orca":
                    appName = "Messenger";
                    break;
                case "com.reddit.frontpage":
                    appName = "Reddit";
                    break;
                case "com.bereal.ft":
                    appName = "BeReal";
                    break;
                /*case "com.vanced.android.youtube":
                    appName = "YouTube Vanced";
                    break;*/
                default:
                    appName = "App not found";
            }

            FirebaseDatabase.getInstance(FIREBASE_LINK)
                    .getReference("Users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child("Apps")
                    .child(packageName.replace(".", "-"))
                    .child("name")
                    .setValue(appName);
        }

    }

    /*public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }*/

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*@RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }*/

    public void readUser()  {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseDatabase.getInstance(FIREBASE_LINK)
                    .getReference("Users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String username = snapshot.child("name").getValue().toString();
                                String pssw = snapshot.child("password").getValue().toString();
                                String bday = snapshot.child("birth").getValue().toString();
                                String groupName = snapshot.child("groupName").getValue().toString();
                                String email = snapshot.child("email").getValue().toString();
                                user = new User(username, email, pssw, bday, groupName);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        }

    }
}
