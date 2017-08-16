package com.cyberocw.habittodosecretary.alaram;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.MainActivity;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.ui.AlarmDialogNew;
import com.cyberocw.habittodosecretary.alaram.ui.TimerDialog;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.alaram.vo.TimerVO;
import com.cyberocw.habittodosecretary.calendar.CalendarDialog;
import com.cyberocw.habittodosecretary.calendar.CalendarManager;
import com.cyberocw.habittodosecretary.memo.MemoDataManager;
import com.cyberocw.habittodosecretary.memo.MemoFragment;
import com.cyberocw.habittodosecretary.record.RecorderDataManager;
import com.cyberocw.habittodosecretary.util.CommonUtils;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AlarmFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AlarmFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlarmFragment extends Fragment{
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	public Context mCtx;
	private AlarmDataManager mAlarmDataManager;
	private TimerDataManager mTimerDataManager;
	private RecorderDataManager mRecorderDataManager;
	private LinearLayout llWeekOfDayWrap;
	private String mParam1;
	private String mParam2;
	private Calendar mCalendar = null;
	MemoDataManager mMemoDataManager;

	AlarmListAdapterInterface mAlarmAdapter;
	//AlarmExListAdapter mAlarmAdapter;
	TimerListAdapter mTimerAdapter;
	CalendarManager mCalendarManager;
	SharedPreferences mPrefs;
	private int mViewType = Const.ALARM_OPTION.SET_DATE_TIMER;
	private int mListViewType = Const.ALARM_LIST_VIEW_TYPE.EXPENDABLE_LIST; // 1: listview 2: expendable view
	private int mMode = -1;
	private long mAlarmId = -1;


	private OnFragmentInteractionListener mListener;
	private View mView;
	TextView mDateTv = null;
	TextView mTvListTitle = null;

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param param1 Parameter 1.
	 * @param param2 Parameter 2.
	 * @return A new instance of fragment MainFragment.
	 */
	public static AlarmFragment newInstance(Context param1, String param2) {
		AlarmFragment fragment = new AlarmFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	public AlarmFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			mMode = getArguments().getInt(Const.PARAM.MODE);
			mAlarmId = getArguments().getLong(Const.PARAM.ALARM_ID);
            Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, " alarm fragment mMode = get Arguments = " + mMode + " al id= " + mAlarmId);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(Const.PARAM.MODE, mMode);
		outState.putLong(Const.PARAM.ALARM_ID, mAlarmId);

		super.onSaveInstanceState(outState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		mView = inflater.inflate(R.layout.fragment_alarm_list, container, false);
		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);

		if(savedInstanceState != null){
			mMode = savedInstanceState.getInt(Const.PARAM.MODE);
			mAlarmId = savedInstanceState.getLong(Const.PARAM.ALARM_ID);
			Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, " alarm fragment mMode = get Arguments = " + mMode + " al id= " + mAlarmId);
		}

		mDateTv = (TextView) mView.findViewById(R.id.dateView);
		mCtx = getActivity();
		mPrefs = mCtx.getSharedPreferences(Const.ALARM_SERVICE_ID, Context.MODE_PRIVATE);
		initActivity();


		File f = new File(mCtx.getFilesDir().getAbsolutePath() + File.separator + "voice");
		File[] list = f.listFiles();
		if(list != null) {
			for (int i = 0; i < list.length; i++) {
				Log.d(Const.DEBUG_TAG, "fileList = " + list[i].getName() + " size=" + list[i].length() / 1000);
			}
		}
	}

	private void initActivity() {
		Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "initActivity started");

		mCalendar = Calendar.getInstance();

		//선택된 날짜 텍스트 지정
		setSelectedDateText(mCalendar);

		llWeekOfDayWrap = (LinearLayout) mView.findViewById(R.id.weekOfDayWrap);
		mAlarmDataManager = new AlarmDataManager(mCtx, mCalendar);
		mTimerDataManager = new TimerDataManager(mCtx);
		mMemoDataManager = new MemoDataManager(mCtx, -1l);
		mCalendarManager = new CalendarManager(mCtx, llWeekOfDayWrap, mCalendar, mDateTv);
		mCalendarManager.setDayClickListener(myDateSetListener);
		mCalendarManager.init();


		mTimerAdapter = new TimerListAdapter(this, mCtx, mTimerDataManager);

		mListViewType = mPrefs.getInt(Const.ALARM_LIST_VIEW_TYPE.TAG, Const.ALARM_LIST_VIEW_TYPE.EXPENDABLE_LIST);
		mTvListTitle = ButterKnife.findById(mView, R.id.tvListTitle);

		Button btnViewMode = ButterKnife.findById(mView, R.id.btnViewMode);
		if(mListViewType == Const.ALARM_LIST_VIEW_TYPE.LIST) {
			btnViewMode.setText(getString(R.string.alarm_sort_type));
		}
		else
			btnViewMode.setText(getString(R.string.alarm_sort_type_time));

		if(mViewType == Const.ALARM_OPTION.SET_DATE_TIMER)
			initAlamUi();
		else
			initTimerUi();

		mAlarmDataManager.resetMinAlarmCall(Const.ALARM_DATE_TYPE.REPEAT);

		if(mMode == Const.ALARM_INTERFACE_CODE.ALARM_POSTPONE_DIALOG && mAlarmId > -1){
			showPostponeAlarmDialog(mAlarmId);
			mMode = -1;
			mAlarmId = -1;
		}
		bindEvent();

		CommonUtils.logCustomEvent("AlarmFragment", "1", "today alarmDataCount", mAlarmDataManager.getCount());

	}

	private void showPostponeAlarmDialog(final long id){

		Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "makeAlarmPostpone");

		AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
		builder.setTitle(getString(R.string.alarm_postpone_title));

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_postpone, null);

		builder.setView(view);

		final NumberPicker npHour = (NumberPicker) view.findViewById(R.id.addAlarmHourPicker);
		final NumberPicker npMinute = (NumberPicker) view.findViewById(R.id.addAlarmMinutePicker);
		npHour.setMaxValue(24);
		npHour.setMinValue(0);
		npMinute.setMaxValue(59);
		npMinute.setMinValue(0);

		Button btn10minute = (Button) view.findViewById(R.id.btn10minute);
		Button btn20minute = (Button) view.findViewById(R.id.btn20minute);
		Button btn30minute = (Button) view.findViewById(R.id.btn30minute);
		Button btn12hour = (Button) view.findViewById(R.id.btn12hour);
		Button btnReset = (Button) view.findViewById(R.id.btnResetTime);
		Button btnRandomTime = (Button) view.findViewById(R.id.btnRandomTime);

		btn10minute.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				npHour.setValue(0);
				npMinute.setValue(10);
			}
		});
		btn20minute.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				npHour.setValue(0);
				npMinute.setValue(20);
			}
		});
		btn30minute.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				npHour.setValue(0);
				npMinute.setValue(30);
			}
		});
		btn12hour.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				npHour.setValue(12);
				npMinute.setValue(0);
			}
		});

		btnReset.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				npHour.setValue(0);
				npMinute.setValue(0);
			}
		});

		btnRandomTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Random random = new Random();

				npHour.setValue(random.nextInt(25));
				npMinute.setValue(random.nextInt(60));
			}
		});

		final AlarmVO alarmVO = mAlarmDataManager.getItemByIdInDB(id);

		if(alarmVO == null){
			Log.e(Const.DEBUG_TAG, "Alarm Id가 잘못되었습니다. 데이터를 가져올 수 없습니다 id=" + id);
			Toast.makeText(mCtx, getString(R.string.alarm_postpone_msg_invalid_id) + " id=" + id, Toast.LENGTH_LONG);
			return;
		}
		alarmVO.setId(-1);

		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id2) {

				//0분 하나만 지정, 모든 날짜에 울려야해서 공휴일 옵션 0
				ArrayList<Integer> arrAlarmCall = new ArrayList<Integer>();
				arrAlarmCall.add(0);
				alarmVO.setAlarmCallList(arrAlarmCall);
				alarmVO.setAlarmDateType(Const.ALARM_DATE_TYPE.POSTPONE_DATE);
				alarmVO.setIsHolidayALL(0);
				alarmVO.setIsHolidayNone(0);
				alarmVO.setRepeatDay(null);

				//알림 날짜 계산
				ArrayList<Calendar> alarmDate = new ArrayList<Calendar>();
				Calendar now = Calendar.getInstance();
				now.add(Calendar.HOUR_OF_DAY, npHour.getValue());
				now.add(Calendar.MINUTE, npMinute.getValue());
				alarmDate.add(now);
				alarmVO.setAlarmDateList(alarmDate);
				alarmVO.setHour(now.get(Calendar.HOUR_OF_DAY));
				alarmVO.setMinute(now.get(Calendar.MINUTE));

				alarmVO.setAlarmTitle(alarmVO.getAlarmTitle());

				// 알람 추가
				if(mAlarmDataManager.addItem(alarmVO) == true) {
					//copyFile하자
					Log.d(Const.DEBUG_TAG, "id ocwocw = " + id + " fullpath ="+CommonUtils.getRecordFullPath(mCtx, id));
					boolean result = getRecorderDataManager().saveFile(CommonUtils.getRecordFullPath(mCtx, id), alarmVO.getId() + ".wav");
					if(result){
						Log.d(this.toString(), "미디어 복사 성공");
						//getRecorderDataManager().deleteRecordFile(oriAlarmId);
					}

					Toast.makeText(mCtx, getString(R.string.success), Toast.LENGTH_LONG).show();
				}
				else
					Toast.makeText(mCtx, getString(R.string.msg_failed_insert), Toast.LENGTH_LONG).show();

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}finally {
					getActivity().finish();
				}

				mAlarmDataManager.resetMinAlarmCall();
				//refreshAlarmList();

			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User cancelled the dialog
				dialog.dismiss();
				getActivity().finish();
			}
		});

// 3. Get the AlertDialog from create()
		AlertDialog dialog = builder.create();
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
				getActivity().finish();
			}
		});

		dialog.show();

		/*

		FragmentManager fm;
		fm = getFragmentManager();

		AlarmDialogNew alarmDialogNew = new AlarmDialogNew();

		if(id != -1) {
			Bundle bundle = new Bundle();

			bundle.putSerializable(Const.ALARM_VO, mAlarmDataManager.getItemByIdInList(id));


			if(mMode == Const.ALARM_INTERFACE_CODE.ALARM_POSTPONE_DIALOG){
				Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, " put mode ok " ) ;
				bundle.putInt(Const.PARAM.MODE, mMode);
			}
			alarmDialogNew.setArguments(bundle);
		}

		alarmDialogNew.show(fm, "fragment_dialog_alarm_add");
		alarmDialogNew.setTargetFragment(this, Const.ALARM_INTERFACE_CODE.ADD_ALARM_CODE);
		*/
	}

	private void initTimerUi(){
		mTvListTitle.setText("Timer List");

		if(mListViewType != Const.ALARM_LIST_VIEW_TYPE.LIST) {
			LinearLayout ll = (LinearLayout) mView.findViewById(R.id.alarmListViewWrap);
			ll.removeAllViews();
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View v = inflater.inflate(R.layout.list_view, null);
			ListView newLv = (ListView) v.findViewById(R.id.alramListView);
			newLv.removeAllViewsInLayout();
			((ViewGroup)newLv.getParent()).removeView(newLv);
			ll.addView(newLv);
		}

		LinearLayout wekkWrap = ButterKnife.findById(mView, R.id.weekDayWrap);
		wekkWrap.setVisibility(View.GONE);

		ListView lv = (ListView) mView.findViewById(R.id.alramListView);


		lv.removeAllViewsInLayout();
		lv.setAdapter(mTimerAdapter);
		mTimerAdapter.refereshStartedTimerId();
		lv.setClickable(true);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				showNewTimerDialog(id);
			}
		});

		lv.setLongClickable(true);
		lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				longClickPopup(position, id);
				return true;
			}
		});

		CommonUtils.logCustomEvent("AlarmFragment Timer", "1");

	}
	private void initAlamUi(){
		mTvListTitle.setText("Alarm List");

		LinearLayout ll = (LinearLayout) mView.findViewById(R.id.alarmListViewWrap);
		ll.removeAllViews();
		LayoutInflater inflater = getActivity().getLayoutInflater();

		Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "initAlamUi start");

		LinearLayout wekkWrap = ButterKnife.findById(mView, R.id.weekDayWrap);
		wekkWrap.setVisibility(View.VISIBLE);

		if(mListViewType == Const.ALARM_LIST_VIEW_TYPE.LIST){

			mAlarmAdapter = new AlarmListAdapter(this, mCtx, mAlarmDataManager);

			View v = inflater.inflate(R.layout.list_view, null);
			ListView newLv = (ListView) v.findViewById(R.id.alramListView);
			/*newLv.removeAllViewsInLayout();*/
			((ViewGroup)newLv.getParent()).removeView(newLv);

			newLv.setAdapter((ListAdapter) mAlarmAdapter);
			ll.addView(newLv);

			newLv.setClickable(true);
			newLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					showNewAlarmDialog(id);
				}
			});

			newLv.setLongClickable(true);
			/*newLv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					longClickPopup(position, id);
					return true;
				}
			});
			*/
		}else{
			//종류별 ui
			mAlarmAdapter = new AlarmExListAdapter(this, mCtx, mAlarmDataManager);

			View v = inflater.inflate(R.layout.expandable_list_view, null);
			ExpandableListView newLv = (ExpandableListView) v.findViewById(R.id.alramListView);
/*
			newLv.removeAllViewsInLayout();
*/
			((ViewGroup)newLv.getParent()).removeView(newLv);

			newLv.setAdapter((ExpandableListAdapter) mAlarmAdapter);
			ll.addView(newLv);

			newLv.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
				@Override
				public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long id) {
					if(groupPosition != 0)
						return false;

					SharedPreferences.Editor editor = mPrefs.edit();
					//editor.clear();
					int len = ((ExpandableListAdapter) mAlarmAdapter).getGroupCount();
					if(len > 0 && mAlarmDataManager.positionToGroupCode(0).equals(String.valueOf(Const.ALARM_DATE_TYPE.REPEAT))){
						Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, " prefs get put boolean = " + !expandableListView.isGroupExpanded(0));
						editor.putBoolean(Const.ALARM_LIST_VIEW_TYPE.TAG_REPEAT_EXPAND, !expandableListView.isGroupExpanded(0));
					}

					editor.commit();
					return false;
				}
			});
			newLv.setClickable(true);
			newLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Log.d(Const.DEBUG_TAG, "ex short click id="+id);
					showNewAlarmDialog(id);
				}
			});

			newLv.setLongClickable(true);
			/*newLv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					Log.d(Const.DEBUG_TAG, "ex long click id="+id);
					longClickPopup(position, id);
					return true;
				}
			});*/
		}

		//mAlarmAdapter.notifyDataSetChanged();
		refreshAlarmList();
	}

	public void expandGroupView(){
		ExpandableListView newLv = (ExpandableListView) mView.findViewById(R.id.alramListView);

		boolean isExpandRepeat = mPrefs.getBoolean(Const.ALARM_LIST_VIEW_TYPE.TAG_REPEAT_EXPAND, true);

		int cnt = ((ExpandableListAdapter) mAlarmAdapter).getGroupCount();

		for(int i = 0 ; i < cnt; i++) {
			if(i == 0 && cnt > 1 && mAlarmDataManager.positionToGroupCode(0).equals(String.valueOf(Const.ALARM_DATE_TYPE.REPEAT)) && isExpandRepeat == false)
				newLv.collapseGroup(0);
			else
				newLv.expandGroup(i);
		}
	}

	public void longClickPopup(int position, final long _id){
		String names[] ={getString(R.string.edit),getString(R.string.delete)};
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mCtx);

		ListView lv = new ListView(mCtx);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		alertDialog.setView(lv);
		alertDialog.setTitle(getString(R.string.option));

		lv.setLayoutParams(params);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mCtx,android.R.layout.simple_list_item_1,names);
		lv.setAdapter(adapter);

		final DialogInterface dialogInterface = alertDialog.show();

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
					case 0:
						if (mViewType == Const.ALARM_OPTION.SET_DATE_TIMER)
							showNewAlarmDialog(_id);
						else
							showNewTimerDialog(_id);

						break;
					case 1:
						deleteItemAlertDialog(_id);
				}
				dialogInterface.dismiss();
			}
		});


	}

	public void deleteItemAlertDialog(final long id){
		AlertDialog.Builder alert_confirm = new AlertDialog.Builder(mCtx);
		alert_confirm.setMessage(getString(R.string.fragment_alarm_msg_delete_confirm)).setCancelable(false).setPositiveButton(getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(mViewType == Const.ALARM_OPTION.SET_DATE_TIMER)
							deleteAlarm(id);
						else
							deleteTimer(id);

						dialog.dismiss();
					}
				}).setNegativeButton(getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// 'No'
						dialog.dismiss();
					}
				});
		AlertDialog alert = alert_confirm.create();
		alert.show();

	}

	public void deleteTimer(long id){
		TimerVO vo = mTimerDataManager.getItemById(id);
		mTimerDataManager.deleteItemById(id);
		mTimerAdapter.notifyDataSetChanged();
	}

	public void deleteAlarm(final long id){
		Crashlytics.log(Log.DEBUG, this.toString(), " getItemByIdInList id = " + id);
		AlarmVO vo = mAlarmDataManager.getItemByIdInList(id);

		Crashlytics.log(Log.DEBUG, this.toString(), " getItemByIdInList vo = " + vo);

		if(vo == null) {
			Toast.makeText(mCtx, "알람 데이터가 없습니다", Toast.LENGTH_SHORT).show();
			return;
		}

		mAlarmDataManager.deleteItemById(id);
		if(vo.getAlarmOption() == Const.ALARM_OPTION_TO_SOUND.RECORD){
			getRecorderDataManager().deleteRecordFile(vo.getId());
		}
		mAlarmDataManager.resetMinAlarmCall();
		refreshAlarmList();
	}

	public void selectedDateChange(Calendar cal){
		myDateSetListener.onDateSet(null, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
	}

	public DatePickerDialog.OnDateSetListener myDateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			mCalendar.set(year, monthOfYear, dayOfMonth);
			setSelectedDateText(year, monthOfYear, dayOfMonth);
			//mCalendarManager.renderDayNum();
			refreshAlarmList();
		}
	};

	public void setSelectedDateText(Calendar calendar){
		setSelectedDateText(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
	}

	public void setSelectedDateText(int year, int monthOfYear, int dayOfMonth){
		mDateTv.setText(String.valueOf(year) + "/" + CommonUtils.numberDigit(2, monthOfYear + 1) + "/" + CommonUtils.numberDigit(2, dayOfMonth));
	}

	public void refreshTimerList(){
		mTimerDataManager.makeDataList();
	}

	public void refreshAlarmList(){
		mAlarmDataManager.makeDataList(mCalendar);
		mAlarmAdapter.notifyDataSetChanged();
		mCalendarManager.renderDayNum();
	}

	public void bindEvent(){
		final Fragment targetFragment = this;
		mDateTv.setOnClickListener(new View.OnClickListener() {
			                           @Override
			                           public void onClick(View v) {
										   if(mViewType == Const.ALARM_OPTION.NO_DATE_TIMER)
										   		return;
										   CalendarDialog d = new CalendarDialog();
										   Bundle bundle = new Bundle();
										   bundle.putString("selectedDate", CommonUtils.convertDateType(mCalendar));
										   d.setArguments(bundle);


										   d.show(getFragmentManager(), "calendarDialog");
										   d.setTargetFragment(targetFragment, Const.ALARM_INTERFACE_CODE.SELECT_CALENDAR_DATE);
			                           }
		                           }
		);
		FloatingActionButton fab = (FloatingActionButton) mView.findViewById(R.id.fabAddAlarm);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mViewType == Const.ALARM_OPTION.SET_DATE_TIMER)
					showNewAlarmDialog(-1);
				else
					showNewTimerDialog(-1);
			}
		});

		//today 오늘
		Button btnToday = (Button) mView.findViewById(R.id.btnToday);
		btnToday.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mViewType == Const.ALARM_OPTION.NO_DATE_TIMER)
					return;
				selectedDateChange(Calendar.getInstance());
			}
		});

		//toggle alarm view
		Button btnToggleViewTimer = (Button) mView.findViewById(R.id.btnToggleViewTimer);

		btnToggleViewTimer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeAlarmView(v);
			}
		});

		//nextWeek
		Button btnNextWeek = (Button) mView.findViewById(R.id.btnNextWeek);
		btnNextWeek.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int dow = mCalendar.get(Calendar.DAY_OF_WEEK);
				mCalendar.add(Calendar.DAY_OF_MONTH, 8 - dow);
				selectedDateChange(mCalendar);
			}
		});
		//prevWeek
		Button btnPrevWeek = (Button) mView.findViewById(R.id.btnPrevWeek);
		btnPrevWeek.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int dow = mCalendar.get(Calendar.DAY_OF_WEEK);
				mCalendar.add(Calendar.DAY_OF_MONTH, -1 * dow);
				selectedDateChange(mCalendar);
			}
		});

		Button btnViewMode = (Button) mView.findViewById(R.id.btnViewMode);
		btnViewMode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mViewType == Const.ALARM_OPTION.NO_DATE_TIMER)
					return;

				if(mListViewType == Const.ALARM_LIST_VIEW_TYPE.LIST) {
					mListViewType = Const.ALARM_LIST_VIEW_TYPE.EXPENDABLE_LIST;
					((Button) v).setText(getString(R.string.alarm_sort_type_time));
				}
				else {
					mListViewType = Const.ALARM_LIST_VIEW_TYPE.LIST;
					((Button) v).setText(getString(R.string.alarm_sort_type));
				}
				SharedPreferences.Editor editor = mPrefs.edit();
				//editor.clear();
				editor.putInt(Const.ALARM_LIST_VIEW_TYPE.TAG, mListViewType);
				editor.commit();

				//initActivity();
				initAlamUi();
			}
		});

	}

	public void changeAlarmView(View view){
		Button btnToggleView = (Button) view;

		if(mViewType == Const.ALARM_OPTION.SET_DATE_TIMER) {
			mViewType = Const.ALARM_OPTION.NO_DATE_TIMER;
			btnToggleView.setText(mCtx.getResources().getString(R.string.btnToggleViewNoTimer));
			initTimerUi();
		}
		else {
			mViewType = Const.ALARM_OPTION.SET_DATE_TIMER;
			btnToggleView.setText(mCtx.getResources().getString(R.string.btnToggleViewTimer));
			initAlamUi();
		}
	}
	public void showNewTimerDialog(long id) {
		FragmentManager fm;
		fm = getFragmentManager();

		TimerDialog timerDialog = new TimerDialog();

		if(id != -1) {
			Bundle bundle = new Bundle();
			bundle.putSerializable(Const.PARAM.TIMER_VO, mTimerDataManager.getItemById(id));
			timerDialog.setArguments(bundle);
		}
		//timerDialog.show(fm, "fragment_dialog_timer");

		timerDialog.setTargetFragment(this, Const.ALARM_INTERFACE_CODE.ADD_TIMER_CODE);

		FragmentTransaction transaction = fm.beginTransaction();
		// For a little polish, specify a transition animation
		transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
		// To make it fullscreen, use the 'content' root view as the container
		// for the fragment, which is always the root view for the activity
		transaction.replace(R.id.warpContainer, timerDialog, "timerDialog")
				.addToBackStack(null).commit();
	}

	public void showNewAlarmDialog(long id) {
		FragmentManager fm;
		fm = getFragmentManager();

		AlarmDialogNew alarmDialogNew = new AlarmDialogNew();

		if(id != -1) {
			Bundle bundle = new Bundle();
			AlarmVO alarmVO = mAlarmDataManager.getItemByIdInList(id);
			if(alarmVO == null){
				Toast.makeText(mCtx, "해당 알람이 데이터베이스에 없습니다.", Toast.LENGTH_SHORT).show();
				return;
			}
			if(alarmVO.getEtcType() != null && alarmVO.getEtcType().equals(Const.ETC_TYPE.MEMO)){
				bundle.putSerializable(Const.PARAM.MEMO_VO, mMemoDataManager.getItemById(alarmVO.getRfid()));
			}
			bundle.putSerializable(Const.PARAM.ALARM_VO, alarmVO);
			alarmDialogNew.setArguments(bundle);
		}

		alarmDialogNew.setTargetFragment(this, Const.ALARM_INTERFACE_CODE.ADD_ALARM_CODE);

		FragmentTransaction transaction = fm.beginTransaction();
		// For a little polish, specify a transition animation
		transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
		// To make it fullscreen, use the 'content' root view as the container
		// for the fragment, which is always the root view for the activity
		transaction.replace(R.id.warpContainer, alarmDialogNew, "alarmDialog")
				.addToBackStack(null).commit();
		//alarmDialogNew.show(fm, "fragment_dialog_alarm_add");
	}

	private RecorderDataManager getRecorderDataManager(){
		if(mRecorderDataManager == null)
			mRecorderDataManager = new RecorderDataManager(mCtx);
		return mRecorderDataManager;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		AlarmVO vo;
		TimerVO tvo;

		mAlarmDataManager.hasContext(getActivity());

		switch(resultCode) {
			case Const.ALARM_INTERFACE_CODE.ADD_ALARM_FINISH_CODE :
				try {
					Bundle bundle = data.getExtras();
					vo = (AlarmVO) bundle.getSerializable(Const.PARAM.ALARM_VO);

					// 알람 추가
					if (!mAlarmDataManager.addItem(vo) == true)
						Toast.makeText(mCtx, getString(R.string.msg_failed_insert), Toast.LENGTH_LONG).show();
					else if (vo.getAlarmOption() == Const.ALARM_OPTION_TO_SOUND.RECORD){
						String fromPath = bundle.getString(Const.PARAM.FILE_PATH);
						if(fromPath == null){
							Toast.makeText(mCtx, "음성 파일이 저장 되지 않았습니다", Toast.LENGTH_SHORT).show();
							return ;
						}
						boolean result = getRecorderDataManager().saveFile(fromPath, vo.getId() + ".wav");
						if(result){
							Log.d(this.toString(), "미디어 복사 성공");
						}
					}
				}catch(Exception e){
					Toast.makeText(mCtx, getString(R.string.msg_failed_insert), Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}

				mAlarmDataManager.resetMinAlarmCall();
				refreshAlarmList();

				break;

			case Const.ALARM_INTERFACE_CODE.ADD_ALARM_MODIFY_FINISH_CODE :
				Bundle bundle = data.getExtras();
				vo = (AlarmVO) bundle.getSerializable(Const.PARAM.ALARM_VO);
				long oriAlarmId = vo.getId();
				String fromPath = bundle.getString(Const.PARAM.FILE_PATH, null);
				// 알람 추가
				if(!mAlarmDataManager.modifyItem(vo) == true)
					Toast.makeText(mCtx, getString(R.string.msg_failed_modify), Toast.LENGTH_LONG).show();
				else if (vo.getAlarmOption() == Const.ALARM_OPTION_TO_SOUND.RECORD){
					//String oriPath = CommonUtils.getRecordFullPath(mCtx, oriAlarmId);

					if(fromPath == null){
						Toast.makeText(mCtx, "음성 파일이 저장 되지 않았습니다", Toast.LENGTH_SHORT).show();
						return ;
					}
					boolean result = getRecorderDataManager().saveFile(fromPath, vo.getId() + ".wav");
					if(result){
						Log.d(this.toString(), "미디어 복사 성공");
						getRecorderDataManager().deleteRecordFile(oriAlarmId);
					}
				}
				//type 변경으로 기존 파일 제거
				else if(fromPath != null){
					getRecorderDataManager().deleteRecordFile(oriAlarmId);
				}
				// 수정일 경우 date type이 변경 될 수도 있기 때문에 두개 모두 갱신
				mAlarmDataManager.resetMinAlarmCall();
				refreshAlarmList();
				break;

			case Const.ALARM_INTERFACE_CODE.ADD_TIMER_FINISH_CODE :
				tvo = (TimerVO) data.getExtras().getSerializable("timerVO");
				// 알람 추가
				if(mTimerDataManager.addItem(tvo) == true)
					mTimerAdapter.notifyDataSetChanged();
				else
					Toast.makeText(mCtx, getString(R.string.msg_failed_insert), Toast.LENGTH_LONG).show();
				break;
			case Const.ALARM_INTERFACE_CODE.ADD_TIMER_MODIFY_FINISH_CODE :
				tvo = (TimerVO) data.getExtras().getSerializable("timerVO");
				if(mTimerDataManager.modifyItem(tvo) == true)
					mTimerAdapter.notifyDataSetChanged();
				else
					Toast.makeText(mCtx, getString(R.string.msg_failed_modify), Toast.LENGTH_LONG).show();
				break;
			case Const.ALARM_INTERFACE_CODE.SELECT_CALENDAR_DATE :
				Calendar date = (Calendar) data.getExtras().getSerializable("selectedDate");
				selectedDateChange(date);
				break;
			case Const.MEMO.MEMO_INTERFACE_CODE.VIEW_MEMO_ETC_CODE :
				//vo = (AlarmVO) data.getExtras().getSerializable(Const.PARAM.ALARM_VO);
				getActivity().getSupportFragmentManager().popBackStackImmediate();
				showMemo(data.getExtras().getLong(Const.PARAM.ALARM_ID));
		}

	}

	public void showMemo(long alarmId){
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString(Const.PARAM.ETC_TYPE_KEY, Const.ETC_TYPE.MEMO);
		bundle.putLong(Const.PARAM.ALARM_ID, alarmId);
		intent.putExtras(bundle);
		((MainActivity)getActivity()).initMainActivity(intent);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public void onDestroy() {
		if(mAlarmDataManager != null)
			mAlarmDataManager.close();
		super.onDestroy();
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		void onFragmentInteraction(Uri uri);
	}


}
