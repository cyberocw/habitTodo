package com.cyberocw.habittodosecretary;

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
	public static final String REQ_CODE = "reqCode";
	public static final String REQ_CODE_REPEAT = "reqCodeRepeat";
	public static final String ALARM_VO = "alarmVO";
	public static final String MEMO_VO = "memoVO";
	public static final String TIMER_VO = "timerVO";
	public static final String VIEW_TYPE = "viewType";
	public static final int ONGOING_TIMER_NOTI_ID = 999999999;
	public static final int ONGOING_ALARM_NOTI_ID = 999999998;


	public class PARAM{
		public static final String MODE = "mode";
		public static final String ALARM_ID = "alarmId";

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
		private static final String[] arrDayName = {"반복-요일", "반복-매달",  "날짜 지정", "내일", "모레"};
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
		public static String getText(int i){
			return arrDayName[i];
		}
		public static String[] getTextList(){
			return arrDayName;
		}
	}

	public class ALARM_OPTION{
		public static final int SET_DATE_TIMER = 0;
		public static final int NO_DATE_TIMER = 1;
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

		public class MEMO_INTERFACE_CODE{
			public static final int ADD_MEMO_CODE = 1111;
			public static final int ADD_MEMO_FINISH_CODE = 1112;
			public static final int ADD_MEMO_MODIFY_FINISH_CODE = 1113;
			public static final int ADD_MEMO_ETC_CODE = 1114;
			public static final String ADD_MEMO_ETC_KEY = "memoEtc";
			public static final String SHARE_MEMO_MODE = "MEMO_MODE";
		}
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
		public static final String IS_NOTIBAR_USE = "isNotibarUse";
		public static final String IS_BACKGROUND_NOTI_USE = "isBackgroundNotibarUse";
		public static final String TTS_VOLUME = "ttsVolume";




	}


}
