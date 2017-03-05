package com.cyberocw.habittodosecretary.alaram;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.alaram.vo.TimerVO;
import com.cyberocw.habittodosecretary.alaram.ui.AlarmDialogNew;
import com.cyberocw.habittodosecretary.alaram.ui.TimerDialog;
import com.cyberocw.habittodosecretary.calendar.CalendarManager;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import static android.util.Log.d;


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
	private LinearLayout llWeekOfDayWrap;
	private String mParam1;
	private String mParam2;
	private Calendar mCalendar = null;
	AlarmListAdapter mAlarmAdapter;
	TimerListAdapter mTimerAdapter;
	CalendarManager mCalendarManager;
	SharedPreferences mPrefs;
	private int mViewType;
	private int mMode = -1;
	private long mAlarmId = -1;

	private OnFragmentInteractionListener mListener;
	private View mView;
	TextView mDateTv = null;

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
            Log.d(Const.DEBUG_TAG, " alarm fragment mMode = get Arguments = " + mMode + " al id= " + mAlarmId);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		mDateTv = (TextView) mView.findViewById(R.id.dateView);
		mCtx = getActivity();
		mPrefs = mCtx.getSharedPreferences(Const.ALARM_SERVICE_ID, Context.MODE_PRIVATE);
		initActivity();
	}

	private void initActivity() {
		Log.d(Const.DEBUG_TAG, "initActivity started");

		bindEvent();
		mCalendar = Calendar.getInstance();

		//viewType 기본값 - 날짜지정 뷰
		mViewType = mPrefs.getInt(Const.VIEW_TYPE, Const.ALARM_OPTION.SET_DATE_TIMER);

		//선택된 날짜 텍스트 지정
		setSelectedDateText(mCalendar);

		llWeekOfDayWrap = (LinearLayout) mView.findViewById(R.id.weekOfDayWrap);

		mCalendarManager = new CalendarManager(mCtx, llWeekOfDayWrap, mCalendar, mDateTv);
		mCalendarManager.setDayClickListener(myDateSetListener);
		mCalendarManager.init();
		mAlarmDataManager = new AlarmDataManager(mCtx, mCalendar);
		mTimerDataManager = new TimerDataManager(mCtx);
		//Calendar date, String title, String[] repeatDay, String type
		mAlarmAdapter = new AlarmListAdapter(this, mCtx, mAlarmDataManager);
		mTimerAdapter = new TimerListAdapter(this, mCtx, mTimerDataManager);

		if(mViewType == Const.ALARM_OPTION.SET_DATE_TIMER)
			initAlamUi();
		else
			initTimerUi();

		mAlarmDataManager.resetMinAlarmCall(Const.ALARM_DATE_TYPE.REPEAT);

		//Toast.makeText(mCtx, "mMode = " + mMode + " alarm id = "+ mAlarmId, Toast.LENGTH_LONG).show();

		Log.d(Const.DEBUG_TAG, "mMode = " + mMode + " alarm id = "+ mAlarmId);

		if(mMode == Const.ALARM_INTERFACE_CODE.ALARM_POSTPONE_DIALOG && mAlarmId > -1){
			showPostponeAlarmDialog(mAlarmId);
			mMode = -1;
			mAlarmId = -1;
		}
	}

	private void showPostponeAlarmDialog(long id){

		Log.d(Const.DEBUG_TAG, "makeAlarmPostpone");

		AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
		builder.setTitle("알림 연장");

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
			Toast.makeText(mCtx, "Alarm Id가 잘못되었습니다. 데이터를 가져올 수 없습니다 id=" + id, Toast.LENGTH_LONG);
			return;
		}
		alarmVO.setId(-1);

		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

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

				// 알람 추가
				if(mAlarmDataManager.addItem(alarmVO) == true)
					mAlarmAdapter.notifyDataSetChanged();
				else
					Toast.makeText(mCtx, "DB에 삽입하는데 실패했습니다", Toast.LENGTH_LONG).show();

				mAlarmDataManager.resetMinAlarmCall();
				refreshAlarmList();
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User cancelled the dialog

			}
		});

// 3. Get the AlertDialog from create()
		AlertDialog dialog = builder.create();
		dialog.show();

		/*

		FragmentManager fm;
		fm = getFragmentManager();

		AlarmDialogNew alarmDialogNew = new AlarmDialogNew();

		if(id != -1) {
			Bundle bundle = new Bundle();

			bundle.putSerializable(Const.ALARM_VO, mAlarmDataManager.getItemByIdInList(id));


			if(mMode == Const.ALARM_INTERFACE_CODE.ALARM_POSTPONE_DIALOG){
				Log.d(Const.DEBUG_TAG, " put mode ok " ) ;
				bundle.putInt(Const.PARAM.MODE, mMode);
			}
			alarmDialogNew.setArguments(bundle);
		}

		alarmDialogNew.show(fm, "fragment_dialog_alarm_add");
		alarmDialogNew.setTargetFragment(this, Const.ALARM_INTERFACE_CODE.ADD_ALARM_CODE);
		*/
	}

	private void initTimerUi(){
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
	}
	private void initAlamUi(){
		ListView lv = (ListView) mView.findViewById(R.id.alramListView);
		lv.removeAllViewsInLayout();
		lv.setAdapter(mAlarmAdapter);

		lv.setClickable(true);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				showNewAlarmDialog(id);
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

		//mAlarmAdapter.notifyDataSetChanged();
	}

	protected void longClickPopup(int position, final long _id){
		String names[] ={"편집","삭제"};
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mCtx);

		ListView lv = new ListView(mCtx);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		alertDialog.setView(lv);
		alertDialog.setTitle("옵션");

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
		alert_confirm.setMessage("해당 알림을 삭제하시겠습니까?").setCancelable(false).setPositiveButton("확인",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(mViewType == Const.ALARM_OPTION.SET_DATE_TIMER)
							deleteAlarm(id);
						else
							deleteTimer(id);

						dialog.dismiss();
					}
				}).setNegativeButton("취소",
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
		AlarmVO vo = mAlarmDataManager.getItemByIdInList(id);

		if(vo == null) {
			return;
		}

		final int options = vo.getAlarmDateType();
		if(options == Const.ALARM_DATE_TYPE.REPEAT){
			AlertDialog.Builder alert_confirm = new AlertDialog.Builder(mCtx);
			alert_confirm.setMessage("반복되는 알림을 모두 삭제하시겠습니까?").setCancelable(false)
					.setPositiveButton("모두 삭제",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									mAlarmDataManager.deleteItemById(id);
									refreshAlarmList();

								}
					}).setNeutralButton("오늘만 삭제",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//// TODO: 2015-08-30 오늘만 삭제 시 오늘 데이터 기준의 테이블에서 useYn을 no로 처리 하는 방식으로 작성 필요
							Toast.makeText(mCtx, "아직 구현 안함", Toast.LENGTH_SHORT).show();
							return;
						}
					}).setNegativeButton("취소",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// 'No'
							return;
						}
					});
			AlertDialog alert = alert_confirm.create();
			alert.show();
		}
		//오늘 날짜만 삭제 (dateList가 여러개일때 로직 바꿔야 함
		else{
			mAlarmDataManager.deleteItemById(id);
			refreshAlarmList();
		}
	}

	public void selectedDateChange(Calendar cal){
		myDateSetListener.onDateSet(null, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
	}

	public DatePickerDialog.OnDateSetListener myDateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			mCalendar.set(year, monthOfYear, dayOfMonth);
			setSelectedDateText(year, monthOfYear, dayOfMonth);
			mCalendarManager.renderDayNum();
			refreshAlarmList();
		}
	};

	public void setSelectedDateText(Calendar calendar){
		setSelectedDateText(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
	}

	public void setSelectedDateText(int year, int monthOfYear, int dayOfMonth){
		mDateTv.setText(String.valueOf(year) + "년 " + String.valueOf(monthOfYear + 1) + "월 " + String.valueOf(dayOfMonth) + "일");
	}

	public void refreshTimerList(){
		mTimerDataManager.makeDataList();
	}

	public void refreshAlarmList(){
		mAlarmDataManager.makeDataList(mCalendar);
		mAlarmAdapter.notifyDataSetChanged();
	}

	public void bindEvent(){

		mDateTv.setOnClickListener(new View.OnClickListener() {
			                           @Override
			                           public void onClick(View v) {

				                           int myYear = mCalendar.get(Calendar.YEAR);
				                           int myMonth = mCalendar.get(Calendar.MONTH);
				                           int myDay = mCalendar.get(Calendar.DAY_OF_MONTH);

				                           //Dialog dlgDate = new DatePickerDialog(new ContextThemeWrapper(mCtx, android.R.style.Theme_Holo_Light_Dialog), myDateSetListener, myYear,
				                           //myMonth, myDay);
				                           Dialog dlgDate = new DatePickerDialog(mCtx, myDateSetListener, myYear,
						                           myMonth, myDay);

				                           dlgDate.show();
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
				selectedDateChange(Calendar.getInstance());
			}
		});

		//toggle alarm view
		Button btnToggleViewTimer = (Button) mView.findViewById(R.id.btnToggleViewTimer);

		btnToggleViewTimer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeAlarmView();
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

	}

	public void changeAlarmView(){
		if(mViewType == Const.ALARM_OPTION.SET_DATE_TIMER) {
			mViewType = Const.ALARM_OPTION.NO_DATE_TIMER;
			initTimerUi();
		}
		else {
			mViewType = Const.ALARM_OPTION.SET_DATE_TIMER;
			initAlamUi();
		}
	}
	public void showNewTimerDialog(long id) {
		FragmentManager fm;
		fm = getFragmentManager();

		TimerDialog timerDialog = new TimerDialog();

		if(id != -1) {
			Bundle bundle = new Bundle();
			bundle.putSerializable(Const.TIMER_VO, mTimerDataManager.getItemById(id));
			timerDialog.setArguments(bundle);
		}
		timerDialog.show(fm, "fragment_dialog_timer");

		timerDialog.setTargetFragment(this, Const.ALARM_INTERFACE_CODE.ADD_TIMER_CODE);
	}

	public void showNewAlarmDialog(long id) {
		FragmentManager fm;
		fm = getFragmentManager();

		AlarmDialogNew alarmDialogNew = new AlarmDialogNew();

		if(id != -1) {
			Bundle bundle = new Bundle();

			bundle.putSerializable(Const.ALARM_VO, mAlarmDataManager.getItemByIdInList(id));

			alarmDialogNew.setArguments(bundle);
		}

		alarmDialogNew.show(fm, "fragment_dialog_alarm_add");
		alarmDialogNew.setTargetFragment(this, Const.ALARM_INTERFACE_CODE.ADD_ALARM_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		AlarmVO vo;
		TimerVO tvo;

		switch(resultCode) {
			case Const.ALARM_INTERFACE_CODE.ADD_ALARM_FINISH_CODE :
				vo = (AlarmVO) data.getExtras().getSerializable("alarmVO");

				// 알람 추가
				if(mAlarmDataManager.addItem(vo) == true)
					mAlarmAdapter.notifyDataSetChanged();
				else
					Toast.makeText(mCtx, "DB에 삽입하는데 실패했습니다", Toast.LENGTH_LONG).show();

				mAlarmDataManager.resetMinAlarmCall(vo.getAlarmDateType());
				refreshAlarmList();

				break;

			case Const.ALARM_INTERFACE_CODE.ADD_ALARM_MODIFY_FINISH_CODE :
				vo = (AlarmVO) data.getExtras().getSerializable("alarmVO");
				// 알람 추가
				if(mAlarmDataManager.modifyItem(vo) == true)
					mAlarmAdapter.notifyDataSetChanged();
				else
					Toast.makeText(mCtx, "DB를 수정하는데 실패했습니다", Toast.LENGTH_LONG).show();

				// 수정일 경우 date type이 변경 될 수도 있기 때문에 두개 모두 갱신
				refreshAlarmList();
				mAlarmDataManager.resetMinAlarmCall();
				break;

			case Const.ALARM_INTERFACE_CODE.ADD_TIMER_FINISH_CODE :
				tvo = (TimerVO) data.getExtras().getSerializable("timerVO");
				// 알람 추가
				if(mTimerDataManager.addItem(tvo) == true)
					mTimerAdapter.notifyDataSetChanged();
				else
					Toast.makeText(mCtx, "DB에 삽입하는데 실패했습니다", Toast.LENGTH_LONG).show();
				break;
			case Const.ALARM_INTERFACE_CODE.ADD_TIMER_MODIFY_FINISH_CODE :
				tvo = (TimerVO) data.getExtras().getSerializable("timerVO");
				if(mTimerDataManager.modifyItem(tvo) == true)
					mTimerAdapter.notifyDataSetChanged();
				else
					Toast.makeText(mCtx, "DB를 수정하는데 실패했습니다", Toast.LENGTH_LONG).show();
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		mView = inflater.inflate(R.layout.fragment_main1, container, false);
		return mView;
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
