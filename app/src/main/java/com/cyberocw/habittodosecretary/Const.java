package com.cyberocw.habittodosecretary;

import android.content.Context;
import android.media.AudioFormat;

import java.util.Calendar;

/**
 * Created by cyberocw on 2015-08-22.
 */
public class Const {
	public static final String DEBUG_TAG = "HabitToDo Debug";
	public static final String DEBUG_DB_TAG = "HabitToDo DB Debug";

	public static final String ERROR_TAG = "HabitToDo Error";
	public static final String ALARM_SERVICE_ID = "alarmServiceList";
	public static final String TIMER_RUNNING_ID = "runningTimerId";
	public static final String REQ_CODE_REPEAT = "reqCodeRepeat";
	public static final int ONGOING_TIMER_NOTI_ID = 999999999;
	public static final int ONGOING_ALARM_NOTI_ID = 999999998;

    public static final boolean IS_DEBUG = true;

	public class PARAM{
		public static final String MODE = "mode";
		public static final String ALARM_ID = "alarmId";

		public static final String REQ_CODE = "reqCode";
		public static final String ETC_TYPE_KEY = "etcType";
		public static final String TIMER_VO = "timerVO";
		public static final String VIEW_TYPE = "viewType";
		public static final String MEMO_VO = "memoVO";
		public static final String ALARM_VO = "alarmVO";
		public static final String FILE_VO = "fileVO";
		public static final String FILE_VO_LIST = "fileVOList";
		public static final String ALARM_TIME_VO = "alarmTimeVO";
		public static final String ALARM_OPTION = "alarmOption";
		public static final String ALARM_ID_TIME_STAMP = "alarmIdTimeStamp";
		public static final String IS_RECORD = "isRecord";
		public static final String FILE_PATH = "filePath";

		public static final String IS_TODO = "isTodo";
    }

	public class ALARM_INTERFACE_CODE{
		public static final int ADD_ALARM_CODE = 111;
		public static final int ADD_ALARM_FINISH_CODE = 112;
		public static final int ADD_ALARM_MODIFY_FINISH_CODE = 113;

		public static final int ALARM_POSTPONE_DIALOG = 114;

		public static final int ADD_TIMER_CODE = 221;
		public static final int ADD_TIMER_FINISH_CODE = 222;
		public static final int ADD_TIMER_MODIFY_FINISH_CODE = 223;

		public static final int SELECT_CALENDAR_DATE = 224;

	}

	public class FRAGMENT_TAG{
		public static final String DASHBOARD= "DASHBOARD_FRAGMENT";
		public static final String MEMO= "MEMO_FRAGMENT";
		public static final String MEMO_DIALOG= "MEMO_DIALOG";
		public static final String ALARM= "ALARM_FRAGMENT";
		public static final String ALARM_DIALOG= "ALARM_DIALOG";
		public static final String SETTING= "SETTING_FRAGMENT";
		public static final String CATEGORY= "CATEGORY_FRAGMENT";
		public static final String KEYWORD= "KEYWORD_FRAGMENT";
	}

	public class KEY_FIELD{
		public static final int HOUR = 10;
		public static final int MINUTE = 10;
	}

	public static class DAY{
		public static final Integer[] ARR_CAL_DAY = {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY};
	}

	public static class ALARM_DATE_TYPE{
		public static final int REPEAT = 0;
		public static final int SET_DATE = 1;
		public static final int TOMORROW = 2;
		public static final int AFTER_DAY_TOMORROW = 3;
		public static final int POSTPONE_DATE = 4;
		public static final int REPEAT_MONTH = 5;
		private static Integer[] arrDayNameCode = {R.string.group_title_repeat_day, R.string.group_title_repeat_month, R.string.group_title_set_date, R.string.group_title_tomorrow, R.string.group_title_after_day_tomorrow};
		private static final int[] arrDayCode = {REPEAT, REPEAT_MONTH,  SET_DATE, TOMORROW, AFTER_DAY_TOMORROW};
		public static int getNumByPosition(int i){
			return arrDayCode[i];
		}
		public static int getPositionByCode(int code){
			for(int i = 0 ; i < arrDayCode.length; i++){
				if(arrDayCode[i] == code)
					return i;
			}
			throw new NullPointerException();
		}

		public static int getNameCodeByPosition(int code){
			int posi = getPositionByCode(code);
			return arrDayNameCode[posi];

		}

		public void setDayText(Context ctx){

		}

		public static int getText(int i){
			return arrDayNameCode[i];
		}
		public static Integer[] getTextList(){
			return arrDayNameCode;
		}
	}

	public class ALARM_OPTION{
		public static final int SET_DATE_TIMER = 0;
		public static final int NO_DATE_TIMER = 1;
	}

	/*
		이게 실제 사용하는 것
	 */
	public class ALARM_OPTION_TO_SOUND{
		public static final int NONE = 0;
		public static final int TTS = 1;
		public static final int RECORD = 2;
		public static final int FILE = 3;
	}

	public class ALARM_LIST_VIEW_TYPE {
        public static final String TAG = "alarmViewType";
		public static final String TAG_REPEAT_EXPAND = "repeatExpand";
		public static final int LIST = 0;
		public static final int EXPENDABLE_LIST = 1;
	}

	public class ALARM_TYPE{
		public static final int NONE = 0;
		public static final int VIB = 1;
		public static final int ALL = 2;
		public static final int NOSOUND = 3;
	}

	public class UID{
		public static final int HOUR = 55555;
		public static final int MINUTE = 55556;
	}

	public class CATEGORY{
		public static final String TYPE = "category";
		public static final String CATEGORY_ID = "cateId";
		public static final String CATEGORY_TITLE_KEY = "cateTitle";
	}

	public class MEMO{
		public static final String IS_INIT_MEMO_MODE = "isInitMemoMode";
		public static final String ORIGINAL_ALARM_ID_KEY = "originalAlarmId";
		public static final String SHOW_TOOLBAR = "showToolbar";
		public static final String SORT_KEY = "sortKey";
		public static final String SORT_REG_DATE_DESC = "regDateDesc";
		public static final String SORT_REG_DATE_ASC = "regDateAsc";
		public static final String SORT_STAR_DESC = "starDesc";
		public static final String SORT_STAR_ASC = "starAsc";

		public class MEMO_INTERFACE_CODE{
			public static final int ADD_MEMO_CODE = 1111;
			public static final int ADD_MEMO_FINISH_CODE = 1112;
			public static final int ADD_MEMO_MODIFY_FINISH_CODE = 1113;
			public static final int ADD_MEMO_ETC_CODE = 1114;
			public static final int VIEW_MEMO_ETC_CODE = 1115;
			public static final int DEL_MEMO_FINISH_CODE = 1116;
			public static final int PICK_FILE_RESULT_CODE = 1117;
			public static final String ADD_MEMO_ETC_KEY = "memoEtc";
			public static final String VIEW_MEMO_ETC_KEY = "viewMemoEtc";
			public static final String SHARE_MEMO_MODE = "MEMO_MODE";
		}
	}
	public class FILE{
		public static final int TYPE_FILE = 0;
		public static final int TYPE_IMAGE = 1;
	}

	public class ETC_TYPE{
		public static final String NONE = "";
		public static final String WEATHER = "WEATHER";
		public static final String MEMO = "MEMO";
	}

	public class SETTING{

		public static final String PREFS_ID = "settingPrefs";
		public static final String VERSION = "version";
		public static final String IS_ALARM_NOTI = "isAlarmNoti";
		public static final String IS_TTS_NOTI = "isTTSNoti";
		public static final String IS_TTS_NOTI_MANNER = "isTTSNotiManner";
		public static final String IS_DISTURB_MODE = "isDisturbMode";
		public static final String IS_NOTIBAR_USE = "isNotibarUse";
		public static final String IS_BACKGROUND_NOTI_USE = "isBackgroundNotibarUse";
		public static final String IS_SHOW_UPDATE_LOG = "isUpdateLog";
		public static final String TTS_VOLUME = "ttsVolume";
	}

	public class KEYWORD{
		public class API{
			private static final String HOST = "http://61.97.142.3";
			public static final String SUM = HOST + "/summary";
			public static final String LIST = HOST + "/list";
			public static final int CALENDAR_INTERFACE_CODE = 800;

			public class MODE{
				public static final String SUM = "SUM";
				public static final String TIME = "TIME";
			}
		}
		public class PARAM{
			public static final String SUM_PORTAL_KEY = "sumPortal";
			public static final String TIME_PORTAL_KEY = "timePortal";
			public static final String DEFAULT = "default";
			public static final String TOP_RANK = "topRank";
			public static final String NAVER = "NAVER";
			public static final String DAUM = "DAUM";
			public static final String ZUM = "ZUM";
			public static final String PREFS_CACHE = "KEYWORD_CACHE";
			public static final String PREFS = "KEYWORD_PREFS";
			public static final String VIEW_MODE = "viewMode";
		}


	}


	public class RECORDER {
		public static final String CACHE_FILE_NAME = "cacheAudio.pcm";
		public static final int FREQUENCY = 44100;
		public static final int CHANNEL_CONFIGURATION_OUT = AudioFormat.CHANNEL_OUT_MONO;
		public static final int CHANNEL_CONFIGURATION_IN = AudioFormat.CHANNEL_IN_MONO;
		public static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	}

	public static String MIME_TYPE_IMAGE = "image/jpeg";
	public static String MIME_TYPE_AUDIO = "audio/amr";
	public static String MIME_TYPE_VIDEO = "video/mp4";
	public static String MIME_TYPE_SKETCH = "image/png";
	public static String MIME_TYPE_FILES = "file/*";
}
