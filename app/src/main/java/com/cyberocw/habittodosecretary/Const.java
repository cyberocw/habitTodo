package com.cyberocw.habittodosecretary;

import java.util.Calendar;

/**
 * Created by cyberocw on 2015-08-22.
 */
public class Const {
	public static final String DEBUG_TAG = "HabitToDo Debug";
	public static final String ALARM_SERVICE_ID = "alarmServiceList";
	public static final String TIMER_RUNNING_ID = "runningTimerId";
	public static final String REQ_CODE = "reqCode";
	public static final String REQ_CODE_REPEAT = "reqCodeRepeat";
	public static final String ALARM_VO = "alarmVo";
	public static final String MEMO_VO = "memoVo";
	public static final String TIMER_VO = "timerVo";
	public static final String VIEW_TYPE = "viewType";
	public static final int ONGOING_TIMER_NOTI_ID = 999999999;


	public class ALARM_INTERFACE_CODE{
		public static final int ADD_ALARM_CODE = 111;
		public static final int ADD_ALARM_FINISH_CODE = 112;
		public static final int ADD_ALARM_MODIFY_FINISH_CODE = 113;

		public static final int ADD_TIMER_CODE = 221;
		public static final int ADD_TIMER_FINISH_CODE = 222;
		public static final int ADD_TIMER_MODIFY_FINISH_CODE = 223;
	}

	public class KEY_FIELD{
		public static final int HOUR = 10;
		public static final int MINUTE = 10;
	}

	public static class DAY{
		public static final Integer[] ARR_CAL_DAY = {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY};
	}

	public class ALARM_DATE_TYPE{
		public static final int REPEAT = 0;
		public static final int SET_DATE = 1;
		public static final int TOMORROW = 2;
		public static final int AFTER_DAY_TOMORROW = 3;
	}

	public class ALARM_OPTION{
		public static final int SET_DATE_TIMER = 0;
		public static final int NO_DATE_TIMER = 1;
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
	}

	public class MEMO{
		public class MEMO_INTERFACE_CODE{
			public static final int ADD_MEMO_CODE = 1111;
			public static final int ADD_MEMO_FINISH_CODE = 1112;
			public static final int ADD_MEMO_MODIFY_FINISH_CODE = 1113;

		}
	}

	public class ETC_TYPE{
		public static final String NONE = "";
		public static final String WEATHER = "WEATHER";
		public static final String MEMO = "MEMO";
	}
}
