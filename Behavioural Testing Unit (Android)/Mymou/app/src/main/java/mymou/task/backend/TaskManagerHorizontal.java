package mymou.task.backend;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import mymou.R;
import mymou.Utils.CrashReport;
import mymou.Utils.FolderManager;
import mymou.Utils.SoundManager;
import mymou.Utils.UtilsSystem;
import mymou.database.MymouDatabase;
import mymou.database.Session;
import mymou.preferences.PreferencesManager;
import mymou.preferences.PrefsActSystem;
import mymou.task.individual_tasks.Task;
import mymou.task.individual_tasks.TaskColoredGrating;
import mymou.task.individual_tasks.TaskContextSequenceLearning;
import mymou.task.individual_tasks.TaskDiscreteMaze;
import mymou.task.individual_tasks.TaskDiscreteValueSpace;
import mymou.task.individual_tasks.TaskEvidenceAccum;
import mymou.task.individual_tasks.TaskExample;
import mymou.task.individual_tasks.TaskObjectDiscrim;
import mymou.task.individual_tasks.TaskObjectDiscrimCol;
import mymou.task.individual_tasks.TaskPassiveReward;
import mymou.task.individual_tasks.TaskProgressiveRatio;
import mymou.task.individual_tasks.TaskRandomDotMotion;
import mymou.task.individual_tasks.TaskSequentialLearning;
import mymou.task.individual_tasks.TaskSocialVideo;
import mymou.task.individual_tasks.TaskSpatialResponse;
import mymou.task.individual_tasks.TaskTrainingFiveTwoStep;
import mymou.task.individual_tasks.TaskTrainingFourSmallMovingCue;
import mymou.task.individual_tasks.TaskTrainingOneFullScreen;
import mymou.task.individual_tasks.TaskTrainingStaticCue;
import mymou.task.individual_tasks.TaskTrainingThreeMovingCue;
import mymou.task.individual_tasks.TaskTrainingTwoShrinkingCue;
import mymou.task.individual_tasks.TaskWalds;

public class TaskManagerHorizontal extends TaskManager {

}
