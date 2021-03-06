package com.cyberocw.habittodosecretary.alaram;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.db.AlarmDbManager;
import com.cyberocw.habittodosecretary.alaram.receiver.AlarmReceiver;
import com.cyberocw.habittodosecretary.alaram.service.AlarmBackgroudService;
import com.cyberocw.habittodosecretary.alaram.service.NotificationService;
import com.cyberocw.habittodosecretary.alaram.service.ReminderService;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmTimeVO;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.common.vo.FileVO;
import com.cyberocw.habittodosecretary.common.vo.RelationVO;
import com.cyberocw.habittodosecretary.db.CommonRelationDBManager;
import com.cyberocw.habittodosecretary.file.FileDataManager;
import com.cyberocw.habittodosecretary.util.CommonUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.services.events.EnabledEventsStrategy;

/**
 * Created by cyberocw on 2015-08-16.
 */
public class AlarmDataManager {
	AlarmManager mManager;
	FileDataManager mFdm;
	Context mCtx = null;
	AlarmDbManager mDb;

	public Calendar mCalendar = null;
	final int mSdkVersion = Build.VERSION.SDK_INT;
	// 각각의 알람이 저장 됨
	private ArrayList<AlarmVO> dataList = new ArrayList<>();

	private ArrayList<AlarmVO> arrRepeat = new ArrayList<>();
	private ArrayList<AlarmVO> arrSetTime = new ArrayList<>();
	private ArrayList<AlarmVO> arrPostpone = new ArrayList<>();

	private ArrayList<String> arrGroupList = new ArrayList();

	HashMap<String, ArrayList> mGroupMap = new HashMap<String, ArrayList>();

	public AlarmDataManager(Context ctx) {
		this(ctx, Calendar.getInstance());
	}

	public AlarmDataManager(Context ctx, Calendar cal) {
		Fabric.with(ctx, new Crashlytics());
		mCtx = ctx;
		mDb = AlarmDbManager.getInstance(ctx);
		mManager = (AlarmManager) mCtx.getSystemService(Context.ALARM_SERVICE);
		mCalendar = cal;
		this.dataList = mDb.getAlarmList(cal);
		bindRecordFile();
	}
	public void bindRecordFile(){
		if(this.dataList == null)
			return;

		FileDataManager fdm = new FileDataManager(mCtx);
		for(int i = 0 ; i < dataList.size(); i++){
			fdm.makeDataList(Const.ETC_TYPE.ALARM, dataList.get(i).getId());
			dataList.get(i).setFileList(fdm.getDataList());
		}
	}
	public ArrayList<AlarmVO> getDataList() {
		return dataList;
	}

	public void makeDataList(Calendar cal){
		this.dataList = mDb.getAlarmList(cal);
		bindRecordFile();
		makeGroupDataList();
	}

	public void makeDataListDashboard(){
		SharedPreferences prefs = mCtx.getSharedPreferences(Const.ALARM_SERVICE_ID, Context.MODE_PRIVATE);
		String text = prefs.getString(Const.PARAM.ALARM_ID, null);
		String[] arrAlarmId = null;
		if(text != null && !"".equals(text)) {
			arrAlarmId = text.split(",");
			if (arrAlarmId.length == 0)
				arrAlarmId[0] = text;
		}
		this.dataList = new ArrayList<AlarmVO>();
		//Log.d(Const.ERROR_TAG, "arrAlarmId="+arrAlarmId[0]);
		if(arrAlarmId != null) {
			AlarmVO vo = mDb.getAlarmById(Long.valueOf(arrAlarmId[0]));

			if(vo == null)
				return;

			// 이후 알람이 없을 경우, 마지막 알람이 prefs에 남아있음 그래서 검증 필요 지난 알림인지
			int dateType = vo.getAlarmDateType();
			if(dateType == Const.ALARM_DATE_TYPE.REPEAT || dateType == Const.ALARM_DATE_TYPE.REPEAT_MONTH){
				this.dataList.add(vo);
				return;
			}
			ArrayList<Calendar> arrAlarmDate = vo.getAlarmDateList();
			Calendar alarmDate = null;
			if(arrAlarmDate == null && arrAlarmDate.size() == 0)
				return;

			alarmDate = arrAlarmDate.get(0);
			alarmDate.set(Calendar.HOUR_OF_DAY, vo.getHour());
			alarmDate.set(Calendar.MINUTE, vo.getMinute());

			ArrayList<Integer> arrAlarmCall = vo.getAlarmCallList();
			int temp;
			Calendar now = Calendar.getInstance();
			if(arrAlarmCall != null) {
				for (int i = 0; i < arrAlarmCall.size(); i++) {
					temp = arrAlarmCall.get(i);
					Calendar c = (Calendar) alarmDate.clone();
					c.add(Calendar.MINUTE, temp);
					//현재 이후 알림이 있으면 추가하고 종료
					if(now.getTimeInMillis() < c.getTimeInMillis()){
						this.dataList.add(vo);
						return;
					}
				}
			}

			//this.dataList.add(vo);
		}
	}
/*

	public void makeDataListDashboard_backup(){
		int MAX_ALARM_CNT = 1;

		Crashlytics.log(Log.DEBUG, this.toString(), "makeDataListDashboard start");
		SharedPreferences prefs = mCtx.getSharedPreferences(Const.ALARM_SERVICE_ID, Context.MODE_PRIVATE);
		String text = prefs.getString(Const.PARAM.ALARM_ID, null);
		String[] arrAlarmId = null;
		if(text != null && !"".equals(text)) {
			arrAlarmId = text.split(",");
			if (arrAlarmId.length == 0)
				arrAlarmId[0] = text;
		}

		ArrayList<AlarmVO> arr = this.dataList;
		ArrayList<AlarmVO> newArr = new ArrayList<AlarmVO>();
		int addCnt = -1;
		for(int i = 0 ; i< arr.size(); i++){
			if(addCnt == -1 && Arrays.binarySearch(arrAlarmId, String.valueOf(arr.get(i).getId())) > -1){
				addCnt = 0;
			}
			if(addCnt >= 0){
				newArr.add(arr.get(i));
				addCnt++;

				if(addCnt == MAX_ALARM_CNT) {
					break;
				}
			}
		}

		this.dataList = newArr;

		if(addCnt < MAX_ALARM_CNT){
			Calendar today = Calendar.getInstance();
			int whileCnt = 0;
			while (addCnt < MAX_ALARM_CNT && whileCnt < 6) {
				int max = MAX_ALARM_CNT - addCnt;
				ArrayList<AlarmVO> arrList = getTomorrowAlarmList(today);
				for (int i = 0; i < Math.min(arrList.size(), max); i++) {
					this.dataList.add(arrList.get(i));
					addCnt++;
				}
				whileCnt++;
			}
		}

		Log.d(Const.DEBUG_TAG, "this.dataList= size="+this.dataList.size() + " text alarmId =" + text + "  vo = " + this.dataList.get(0).getId());
	}

	private ArrayList<AlarmVO> getTomorrowAlarmList(Calendar calendar){
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		return mDb.getAlarmList(calendar);
	}
*/

	private void makeGroupDataList(){
		resetGroupData();

		AlarmVO vo;

		for(int i = 0 ; i < this.dataList.size(); i++){
			vo = this.dataList.get(i);
			if(vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.REPEAT){
				arrRepeat.add(vo);
			}else if(vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.SET_DATE  || vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.REPEAT_MONTH){
				arrSetTime.add(vo);
			}else{
				arrPostpone.add(vo);
			}
		}

		if(arrRepeat.size() > 0) {
			arrGroupList.add(String.valueOf(Const.ALARM_DATE_TYPE.REPEAT));
			mGroupMap.put(String.valueOf(Const.ALARM_DATE_TYPE.REPEAT), arrRepeat);
		}
		if(arrSetTime.size() > 0) {
			arrGroupList.add(String.valueOf(Const.ALARM_DATE_TYPE.SET_DATE));
			mGroupMap.put(String.valueOf(Const.ALARM_DATE_TYPE.SET_DATE), arrSetTime);
		}
		if(arrPostpone.size() > 0) {
			arrGroupList.add(String.valueOf(Const.ALARM_DATE_TYPE.POSTPONE_DATE));
			mGroupMap.put(String.valueOf(Const.ALARM_DATE_TYPE.POSTPONE_DATE), arrPostpone);
		}
	}



	public void setDataList(ArrayList<AlarmVO> dataList) {
		this.dataList = dataList;
	}

	public int getCount(){
		return this.dataList.size();
	}

	public int getGroupCount(){
		int i = 0;
		if(arrRepeat.size() > 0)
			i++;
		if(arrPostpone.size() > 0)
			i++;
		if(arrSetTime.size() > 0)
			i++;
		return i;
	}

	public AlarmVO getItem(int position){
		return this.dataList.get(position);
	}

	public ArrayList getGroup(int groupPosition){
		String groupCode = positionToGroupCode(groupPosition);

		if(!mGroupMap.containsKey(groupCode))
			return null;

		return mGroupMap.get(groupCode);
	}

	public AlarmVO getGroupItem(int groupPosition, int position){
		ArrayList arrList = this.getGroup(groupPosition);

		if(arrList != null && arrList.size() < position + 1)
			return null;

		return (AlarmVO) arrList.get(position);
	}

	public AlarmVO getItemByIdInList(long id){
		for(int i = 0 ; i < dataList.size() ; i++){
			if(dataList.get(i).getId() == id){
				return dataList.get(i);
			}
		}
		return null;
	}

	public String positionToGroupCode(int position){
		if(arrGroupList.size() == 0)
			return "-1";
		return arrGroupList.get(position);
		/*
		if(position == 0)
			return String.valueOf(Const.ALARM_DATE_TYPE.REPEAT);
		if(position == 1)
			return String.valueOf(Const.ALARM_DATE_TYPE.SET_DATE);
		if(position == 2)
			return String.valueOf(Const.ALARM_DATE_TYPE.POSTPONE_DATE);
		else {
			Toast.makeText(mCtx, "group position 이 잘못되었습니다", Toast.LENGTH_SHORT);
			return "";
		}
		*/
	}
	public String getGroupTitle(int position){
		int groupCode = Integer.parseInt(positionToGroupCode(position));
		String result = "";
		switch (groupCode){
			case Const.ALARM_DATE_TYPE.REPEAT : result = mCtx.getResources().getString(R.string.group_title_repeat); break;
			case Const.ALARM_DATE_TYPE.SET_DATE : result = mCtx.getResources().getString(R.string.group_title_set_date); break;
			case Const.ALARM_DATE_TYPE.POSTPONE_DATE : result = mCtx.getResources().getString(R.string.group_title_postpone); break;
		}
		return result;
	}

	public AlarmVO getItemByIdInDB(long id){
		return mDb.getAlarmById(id);
	}

	public boolean deleteItemById(long id){
		Crashlytics.log(Log.DEBUG, this.toString(), "alarm delete id="+id);
		boolean delResult = mDb.deleteAlarm(id);

		if(delResult == false)
			return false;

		for(int i = 0 ; i < dataList.size() ; i++){
			if(dataList.get(i).getId() == id){
				dataList.remove(i);
				return true;
			}
		}
		return true;
	}

	public boolean deleteItemFileDbReal(AlarmVO vo){
		ArrayList<FileVO> arrFile = vo.getFileList();
		if(arrFile != null && arrFile.size() > 0){
			getFileDataManager().addDeleteItem(arrFile);
			getFileDataManager().deleteAll(Environment.DIRECTORY_RINGTONES);
			return true;
		}
		return false;
	}

	public AlarmVO deleteItemById(int position){
		return dataList.remove(position);
	}

	public AlarmVO addItem(Calendar date, String title, ArrayList<Integer> repeatDay, String type){
		AlarmVO item = new AlarmVO(date, title, repeatDay);
		this.dataList.add(item);
		return item;
	}

	private void addGroupItem(String groupCode, AlarmVO vo){
		mGroupMap.get(groupCode).add(vo);
	}
	private void resetGroupData(){
		mGroupMap = new HashMap<>();
		arrSetTime = new ArrayList<AlarmVO>();
		arrRepeat = new ArrayList<AlarmVO>();
		arrPostpone = new ArrayList<AlarmVO>();
		arrGroupList = new ArrayList();
	}

	public boolean addItem(AlarmVO item){

		mDb.insertAlarm(item);
		//알람 인던트 등록
		if(item.getId() == -1){
			Log.e(Const.DEBUG_TAG, "오류 : 알림 ID가 생성되지 않았습니다");
			Toast.makeText(mCtx, mCtx.getString(R.string.db_failed_generate_id), Toast.LENGTH_LONG).show();
			return false;
		}

		if(item.getRfid() > -1){
			Crashlytics.log(Log.DEBUG, this.toString(), "addItem  mctx = " + mCtx);
			CommonRelationDBManager relationDBManager = CommonRelationDBManager.getInstance(mCtx);
			RelationVO rvo = new RelationVO();
			rvo.setAlarmId(item.getId());
			rvo.setType(item.getEtcType());
			rvo.setfId(item.getRfid());
			relationDBManager.insert(rvo);
		}

		this.dataList.add(item);

		return true;
	}

	/**
	 * alarm을 지웠다가 새로 insert 하는 메서드
	 * relation 또한 지우고 새로 insert 해줌
	 * @param item (AlaramVO)
	 * @return boolean
	 */
	public boolean modifyItem(AlarmVO item){
		//삭제 후 새로 insert, relation 또한 새로 insert 해줌 (일단 하나의 alarm은 하나의 relation만을 가진 상태의 로직)
		long oriId = item.getId();
		boolean delResult = this.deleteItemById(oriId);
		if(delResult == false)
			return false;
		item.setId(-1);
		addItem(item);

		if(item.getId() == -1){
			Log.e(Const.DEBUG_TAG, "오류 : 알림 ID가 생성되지 않았습니다");
			Toast.makeText(mCtx, mCtx.getString(R.string.db_failed_generate_id), Toast.LENGTH_LONG).show();
			return false;
		}

		this.dataList.add(item);
		return true;
	}

	//별도 후처리 안함
	public boolean modifyUseYn(AlarmVO item){
		Log.d(this.toString(), "modifyUseYn start item.getAlarmDateType()=" + item.getAlarmReminderType());
		boolean result = mDb.modifyUse(item);
		if(item.getAlarmDateType() == Const.ALARM_REMINDER_MODE.REMINDER){
			resetReminderNoti();
		}
		return result;
	}

	public void resetMinAlarmCall(){
		//resetMinAlarmCall(Const.ALARM_DATE_TYPE.REPEAT);
		//resetMinAlarmCall(Const.ALARM_DATE_TYPE.SET_DATE);
		resetMinAlarm();
	}

	public void resetMinAlarmCall(int type) {
		resetMinAlarm();
	}
	public void resetMinAlarm(boolean isReceiver){
		PowerManager pm;
		PowerManager.WakeLock wakeLock = null;
		if(isReceiver){
			pm = ((PowerManager) mCtx.getSystemService(Context.POWER_SERVICE));
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "resetMin wakeLock");
			// wakelock 사용
			wakeLock.acquire();
			Crashlytics.log(Log.DEBUG, this.toString(), "resetMin wakeLock acquire");
		}
		try {
			this.resetMinAlarm();
		}
		catch (Exception e){
			e.printStackTrace();
		}
		finally {
			if(isReceiver && wakeLock.isHeld()) {
				wakeLock.release();
				Crashlytics.log(Log.DEBUG, this.toString(), "resetMin wakeLock release");
			}
		}

	}
	public void resetMinAlarm(){
		SharedPreferences prefs = mCtx.getSharedPreferences(Const.ALARM_SERVICE_ID, Context.MODE_PRIVATE);
		stopAllAlarm(prefs);

		//resetReminderNoti();

		SharedPreferences prefsSetting = mCtx.getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
		if(prefsSetting.getBoolean(Const.SETTING.IS_DISTURB_MODE, false)){
			return;
		}

		ArrayList<AlarmTimeVO> alarmTimeList1 = null;
		ArrayList<AlarmTimeVO> alarmTimeList2 = null;
		ArrayList<AlarmTimeVO> alarmTimeList = null;
		String reqCode = Const.PARAM.REQ_CODE;

		alarmTimeList1 = mDb.getMinAlarmTime();

		Calendar cal = Calendar.getInstance();
		int dayNum = cal.get(Calendar.DAY_OF_WEEK); //sun 1 mon 2 ...
		alarmTimeList2 = mDb.getMinRepeatAlarm(dayNum);

		//Log.d(this.toString(), "alarmTimeList2="+alarmTimeList2 + " alarmTimeList2 size="+alarmTimeList2.size());

		int minList = 0;
		long minTimeStamp = 0;
		// setTime 과 repeat 비교하여 더 가까운 시간 것을 지정
		if(alarmTimeList1 != null && alarmTimeList1.size() > 0){
			minList = 1;
			minTimeStamp = alarmTimeList1.get(0).getTimeStamp();
		}
		if(alarmTimeList2 != null && alarmTimeList2.size() > 0){
			if(minTimeStamp == 0 || minTimeStamp > alarmTimeList2.get(0).getTimeStamp())
				minList = 2;
			else if(minTimeStamp == alarmTimeList2.get(0).getTimeStamp())
				minList = 3;
		}
		if(minList == 1)
			alarmTimeList = alarmTimeList1;
		else if(minList == 2)
			alarmTimeList = alarmTimeList2;
		else if(minList == 3){
			alarmTimeList = alarmTimeList1;
			alarmTimeList.addAll(alarmTimeList2);
		}

		//새로 등록
		if(alarmTimeList == null || alarmTimeList.size() == 0) {
			return;
		}

		Crashlytics.log(Log.DEBUG, this.toString(), "alarmTimeList.size()="+alarmTimeList.size());

		String[] arrReq = new String[alarmTimeList.size()];
		Long[] arrAlarmId = new Long[alarmTimeList.size()];

		Calendar tempCal =  Calendar.getInstance();

		for(int i = 0; i < alarmTimeList.size(); i++){
			tempCal.setTimeInMillis(alarmTimeList.get(i).getTimeStamp());
			arrReq[i] = String.valueOf(this.setAlarm(alarmTimeList.get(i)));
			arrAlarmId[i] = alarmTimeList.get(i).getId();
			String aa = "다음 알람은 " + alarmTimeList.get(i).getAlarmTitle()
					+ " 시간:" + CommonUtils.convertFullDateType(tempCal) + " 입니다. id=" + alarmTimeList.get(i).getId();

			CommonUtils.putLogPreference(mCtx, aa);
			Crashlytics.log(Log.DEBUG, this.toString(), aa);
		}

		String newReqCode = TextUtils.join("," , arrReq);
		String alarmIds = TextUtils.join("," , arrAlarmId);

		//등록된 code 저장해둠
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(reqCode);
		editor.remove(Const.PARAM.ALARM_ID);
		editor.putString(reqCode, newReqCode);
		editor.putString(Const.PARAM.ALARM_ID, alarmIds);
		editor.putLong(Const.PARAM.ALARM_ID_TIME_STAMP, alarmTimeList.get(0).getTimeStamp());

		editor.commit();

	}
	public void stopAllAlarm(){
		stopAllAlarm(null);
	}
	public void stopAllAlarm(SharedPreferences prefs){
		if(prefs == null)
			prefs = mCtx.getSharedPreferences(Const.ALARM_SERVICE_ID, Context.MODE_PRIVATE);

		String text = prefs.getString(Const.PARAM.REQ_CODE, null);

		AlarmManager alarmManager = (AlarmManager) mCtx
				.getSystemService(Context.ALARM_SERVICE);
		Intent myIntent = new Intent(mCtx, AlarmReceiver.class);

		Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, " get text = " + text);
		//기존것 취소
		if(text != null && !"".equals(text)){
			String[] arrReq = text.split(",");

			if(arrReq.length == 0)
				arrReq[0] = text;

			for(int i = 0 ; i < arrReq.length; i++){
				boolean alarmUp = (PendingIntent.getBroadcast(mCtx, Integer.valueOf(arrReq[i]), myIntent, PendingIntent.FLAG_NO_CREATE) != null);
				if (alarmUp) {
					PendingIntent sender1 = PendingIntent.getBroadcast(mCtx, Integer.valueOf(arrReq[i]), myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					alarmManager.cancel(sender1);
					sender1.cancel();
				}
			}
			//background 서비스 취소
			Intent intentAlarmbackground = new Intent(mCtx, AlarmBackgroudService.class);
			Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "background Service stop");
			mCtx.stopService(intentAlarmbackground);
		}
	}

	public long setAlarm(AlarmTimeVO alarmTimeVO) {
		Random mRand = new Random();
		int rand = mRand.nextInt(10000);

		int backgroundLimitTime = Const.ALARM_DEFAULT_OPTION.BACKGROUND_LIMIT_TIME;
		int EXACT_LIMIT_TIME = 15;
		if(mSdkVersion < Build.VERSION_CODES.M && mSdkVersion >= Build.VERSION_CODES.KITKAT) {
			Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "backgroundLimitTime = 5");
			backgroundLimitTime = Const.ALARM_DEFAULT_OPTION.BACKGROUND_LIMIT_TIME_OLD;
		}

		Calendar ccc = Calendar.getInstance();
		Calendar nowCal = Calendar.getInstance();
		long reqResult = (alarmTimeVO.getId() + nowCal.get(Calendar.MILLISECOND));

		int reqCode = (int) reqResult + nowCal.get(Calendar.HOUR_OF_DAY) + nowCal.get(Calendar.MINUTE) + rand;
		if(reqCode >= Integer.MAX_VALUE)
			reqCode = reqCode / 10000;

		Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "reqCode=" + reqCode + " alarmTimeVO= getTimeStamp = " + alarmTimeVO.getTimeStamp());
		//myIntent.removeExtra("title");
		long timeStamp = alarmTimeVO.getTimeStamp();
		ccc.setTimeInMillis(timeStamp);
		ccc.set(Calendar.SECOND, 0);
		ccc.set(Calendar.MILLISECOND, 0);

		alarmTimeVO.setReqCode(reqCode);

		//1분 혹은 lollipop 5분 초 이내일 경우 AlarmBackgroudService 돌림
		ccc.add(Calendar.MINUTE, -1 * backgroundLimitTime);
		if(ccc.getTimeInMillis() <= nowCal.getTimeInMillis()){
			Intent myIntent = new Intent(mCtx, AlarmBackgroudService.class);
			myIntent.putExtra(Const.PARAM.ALARM_TIME_VO, alarmTimeVO);

			Crashlytics.log(Log.DEBUG, this.getClass().toString(), "timer background start service alarmVO id=" + alarmTimeVO.getId() + " title =" + alarmTimeVO.getAlarmTitle());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				mCtx.startForegroundService(myIntent);
			} else {
				mCtx.startService(myIntent);
			}
			return reqCode;
		}
		//5분 이상일 경우 setTime 시킴
		AlarmManager alarmDataManager = (AlarmManager) mCtx.getSystemService(Context.ALARM_SERVICE);
		Intent myIntent = new Intent(mCtx, AlarmReceiver.class);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(alarmTimeVO);
			out.flush();
			byte[] data = bos.toByteArray();
			myIntent.putExtra(Const.PARAM.ALARM_TIME_VO, data);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bos.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		//총 -15분 만들어줌
		ccc.add(Calendar.MINUTE, -1 * (EXACT_LIMIT_TIME - backgroundLimitTime));
		boolean isSetAlarmClock = false;

		//Log.d(this.toString(), " calendar diff ccc=" +CommonUtils.convertFullDateType(ccc) + "  now = " + CommonUtils.convertFullDateType(nowCal));

		ccc.set(Calendar.SECOND, 1);
		//15분 이내 알람일 경우 setAlarmClock으로 원래 알람의 backgroundLimitTime 분 전(backgroundLimitTime) 울리도록 함
		if(ccc.getTimeInMillis() <= nowCal.getTimeInMillis()){
			isSetAlarmClock = true;
			ccc.add(Calendar.MINUTE, (EXACT_LIMIT_TIME - backgroundLimitTime));
			if(mSdkVersion >= Build.VERSION_CODES.M) {
				ccc.set(Calendar.SECOND, 0);
			}
		}else{
			//원래 알람의 -14분 전에 울리도록
			ccc.add(Calendar.MINUTE, 1);
			//ccc.add(Calendar.SECOND, 55);
		}

		//myIntent.putExtra("title", alarmTimeVO.getAlarmTitle() + " " + (callTime < 0 ? callTime + "분 전" : (callTime > 0 ? callTime + "분 후" : "")));
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mCtx, reqCode, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		setAlarmExact(alarmDataManager, AlarmManager.RTC_WAKEUP, ccc.getTimeInMillis(), pendingIntent, isSetAlarmClock);

		return reqCode;
	}

	@SuppressLint("NewApi")
	private void setAlarmExact(AlarmManager am, int type, long time, PendingIntent it, boolean isSetAlarmClock){
		final int sdkVersion = Build.VERSION.SDK_INT;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if(isSetAlarmClock) {
				Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "high version set alarmExact setAlarmClock");
				am.setAlarmClock(new AlarmManager.AlarmClockInfo(time, it), it);
			}
			else {
				Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "high version set alarmExact setExactAndAllowWhileIdle");
				am.setExactAndAllowWhileIdle(type, time, it);
			}
		}
		else if(sdkVersion >= Build.VERSION_CODES.KITKAT) {
			Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "kitkat set alarmExact");
			am.setExact(type, time, it);
		}
		else {
			Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "low version set alarm");
			am.set(type, time, it);
		}
	}

	public boolean hasContext(Context ctx){
		if(mCtx == null){
			mCtx = ctx;
			return false;
		}
		else
			return true;
	}

	public void close(){
		mDb.closeDB();
	}

	private FileDataManager getFileDataManager(){
		if(mFdm == null)
			mFdm = new FileDataManager(mCtx);
		return mFdm;
	}

	public void saveFile(AlarmVO vo, File targetFile) {
		FileVO fileVO = new FileVO(Uri.fromFile(targetFile), Const.MIME_TYPE_AUDIO_WAV);
		fileVO.setName(targetFile.getName());
		fileVO.setSize(targetFile.length());
		fileVO.setfId(vo.getId());
		fileVO.setType(Const.ETC_TYPE.ALARM);
		getFileDataManager().addItem(fileVO);
		ArrayList<FileVO> fileList = new ArrayList<FileVO>();
		fileList.add(fileVO);
		vo.setFileList(fileList);
	}

	public void resetReminderNoti(){
		Log.d(this.toString(), "resetReminderNoti start");
		Intent myIntent;
		myIntent = new Intent(mCtx, ReminderService.class);
		myIntent.putExtra(Const.PARAM.MODE, "RESET");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			mCtx.startForegroundService(myIntent);
		}
		else{
			mCtx.startService(myIntent);
		}

		List<AlarmTimeVO> list = mDb.getReminderRunningList();
		AlarmTimeVO vo;
		for(int i = 0; i < list.size(); i++){
			myIntent = new Intent(mCtx, ReminderService.class);
			vo = list.get(i);
			myIntent.putExtra("title", vo.getAlarmTitle());
			myIntent.putExtra(Const.PARAM.ALARM_REMINDER_MODE, vo.getAlarmReminderType());
			myIntent.putExtra(Const.PARAM.ETC_TYPE_KEY, vo.getEtcType());
			myIntent.putExtra(Const.PARAM.REQ_CODE, vo.getReqCode());
			myIntent.putExtra(Const.PARAM.ALARM_ID, vo.getfId());
			myIntent.putExtra(Const.PARAM.CALL_TIME, vo.getCallTime());
			myIntent.putExtra(Const.PARAM.REPEAT_DAY_ID, vo.getRepeatDayId());
			myIntent.putExtra(Const.PARAM.MODE, "REFRESH");

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				mCtx.startForegroundService(myIntent);
			}
			else{
				mCtx.startService(myIntent);
			}
		}
	}

}
