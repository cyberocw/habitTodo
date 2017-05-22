package com.cyberocw.habittodosecretary.alaram;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.db.AlarmDbManager;
import com.cyberocw.habittodosecretary.alaram.receiver.AlarmReceiver;
import com.cyberocw.habittodosecretary.alaram.service.AlarmBackgroudService;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmTimeVO;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.common.vo.RelationVO;
import com.cyberocw.habittodosecretary.db.CommonRelationDBManager;
import com.cyberocw.habittodosecretary.util.CommonUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import io.fabric.sdk.android.Fabric;

/**
 * Created by cyberocw on 2015-08-16.
 */
public class AlarmDataManager {
	AlarmManager mManager;
	Context mCtx = null;
	AlarmDbManager mDb;
	public Calendar mCalendar = null;

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
	}

	public ArrayList<AlarmVO> getDataList() {
		return dataList;
	}

	public void makeDataList(Calendar cal){
		this.dataList = mDb.getAlarmList(cal);
		makeGroupDataList();
	}

	private void makeGroupDataList(){
		resetGroupData();

		AlarmVO vo;

		for(int i = 0 ; i < this.dataList.size(); i++){
			vo = this.dataList.get(i);
			if(vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.REPEAT || vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.REPEAT_MONTH){
				arrRepeat.add(vo);
			}else if(vo.getAlarmDateType() == Const.ALARM_DATE_TYPE.SET_DATE){
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
			Toast.makeText(mCtx, "오류 : 알림 ID가 생성되지 않았습니다", Toast.LENGTH_LONG).show();
			return false;
		}

		if(item.getRfid() > -1){
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
		CommonRelationDBManager relationDBManager = CommonRelationDBManager.getInstance(mCtx);
		RelationVO rvo = relationDBManager.getByAlarmId(oriId);

		boolean delResult = this.deleteItemById(oriId);

		if(delResult == false)
			return false;

		item.setId(-1);

		addItem(item);

		/*
		//변경 된 내용으로 새롭게 등록
		if(item.getRfid() > -1 ){
			rvo.setAlarmId(item.getId());
			rvo.setType(item.getEtcType());
			rvo.setfId(item.getRfid());
			relationDBManager.insert(rvo);
		}
		//기존 정보 재등록
		else if(rvo.getAlarmId() > -1){
			rvo.setAlarmId(item.getId());
			relationDBManager.insert(rvo);
		}
		*/
		//기존 ETC 연계가 있었다면 재등록
		/*
		if(item.getRfid() rvo.getAlarmId() > -1) {
			rvo.setAlarmId(item.getId());
			relationDBManager.insert(rvo);
		}
		*/

		if(item.getId() == -1){
			Log.e(Const.DEBUG_TAG, "오류 : 알림 ID가 생성되지 않았습니다");
			Toast.makeText(mCtx, "오류 : 알림 ID가 생성되지 않았습니다", Toast.LENGTH_LONG).show();
			return false;
		}

		this.dataList.add(item);

		return true;
	}

	public boolean modifyUseYn(AlarmVO item){
		return mDb.modifyUse(item);
	}

	public void resetMinAlarmCall(){
		//resetMinAlarmCall(Const.ALARM_DATE_TYPE.REPEAT);
		//resetMinAlarmCall(Const.ALARM_DATE_TYPE.SET_DATE);
		resetMinAlarm();
	}

	public void resetMinAlarmCall(int type) {
		resetMinAlarm();
	}
	public void resetMinAlarm(){
		ArrayList<AlarmTimeVO> alarmTimeList1 = null;
		ArrayList<AlarmTimeVO> alarmTimeList2 = null;
		ArrayList<AlarmTimeVO> alarmTimeList = null;
		String reqCode = Const.PARAM.REQ_CODE;

		alarmTimeList1 = mDb.getMinAlarmTime();

		Calendar cal = Calendar.getInstance();
		int dayNum = cal.get(Calendar.DAY_OF_WEEK); //sun 1 mon 2 ...
		alarmTimeList2 = mDb.getMinRepeatAlarm(dayNum);

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

		SharedPreferences prefs = mCtx.getSharedPreferences(Const.ALARM_SERVICE_ID, Context.MODE_PRIVATE);
		stopAllAlarm(prefs);

		SharedPreferences prefsSetting = mCtx.getSharedPreferences(Const.SETTING.PREFS_ID, Context.MODE_PRIVATE);
		boolean isUseNotibar = prefsSetting.getBoolean(Const.SETTING.IS_NOTIBAR_USE, true);

		Crashlytics.log(Log.DEBUG, this.toString(), "isUseNotibar=="+isUseNotibar);

		if(!isUseNotibar){
			SharedPreferences.Editor editor = prefs.edit();
			editor.remove(reqCode);
			editor.commit();
			return;
		}

		//새로 등록
		if(alarmTimeList == null)
			return ;

		String[] arrReq = new String[alarmTimeList.size()];
		Long[] arrAlarmId = new Long[alarmTimeList.size()];

		Crashlytics.log(Log.DEBUG, this.toString(), "alarmTimeList.size()="+alarmTimeList.size());
		Calendar tempCal =  Calendar.getInstance();

		for(int i = 0; i < alarmTimeList.size(); i++){
			tempCal.setTimeInMillis(alarmTimeList.get(i).getTimeStamp());
			arrReq[i] = String.valueOf(setAlarm(alarmTimeList.get(i)));
			arrAlarmId[i] = alarmTimeList.get(i).getId();
			String aa = "다음 알람은 " + alarmTimeList.get(i).getAlarmTitle()
					+ " 시간:" + CommonUtils.convertFullDateType(tempCal) + " 입니다. id=" + alarmTimeList.get(i).getId();

			CommonUtils.putLogPreference(mCtx, aa);
			Crashlytics.log(Log.DEBUG, this.toString(), aa);
			//Toast.makeText(mCtx, aa, Toast.LENGTH_SHORT).show();
		}

		String newReqCode = TextUtils.join("," , arrReq);
		String alarmIds = TextUtils.join("," , arrAlarmId);


		Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "newReqCode=" + newReqCode);
		//등록된 code 저장해둠
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(reqCode);
		editor.remove(Const.PARAM.ALARM_ID);
		editor.putString(reqCode, newReqCode);
		editor.putString(Const.PARAM.ALARM_ID, alarmIds);
		editor.putLong(Const.PARAM.ALARM_ID_TIME_STAMP, alarmTimeList.get(0).getTimeStamp());

		editor.commit();
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

			CommonUtils.putLogPreference(mCtx, "기존 알람 모두 취소 완료");
		}
	}

	public long setAlarm(AlarmTimeVO alarmTimeVO) {
		Calendar ccc = Calendar.getInstance();
		Calendar nowCal = Calendar.getInstance();
		long reqResult = (alarmTimeVO.getId() + nowCal.get(Calendar.MILLISECOND));
		if(reqResult >= Integer.MAX_VALUE)
			reqResult = reqResult / 10000;
		int reqCode = (int) reqResult;
		Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, "reqCode=" + reqCode + " alarmTimeVO= getTimeStamp = " + alarmTimeVO.getTimeStamp());
		//myIntent.removeExtra("title");
		long timeStamp = alarmTimeVO.getTimeStamp();
		ccc.setTimeInMillis(timeStamp);

		// ----------------------- Doze 모드 때문에 제공되는 API만 사용
		ccc.add(Calendar.MINUTE, -1);
		//1분 이내
		if(ccc.getTimeInMillis() <= nowCal.getTimeInMillis()){
			Intent myIntent = new Intent(mCtx, AlarmBackgroudService.class);
			alarmTimeVO.setReqCode(reqCode);
			myIntent.putExtra(Const.PARAM.ALARM_TIME_VO, alarmTimeVO);

			Crashlytics.log(Log.DEBUG, this.getClass().toString(), "timer background start service alarmVO id=" + alarmTimeVO.getId() + " title =" + alarmTimeVO.getAlarmTitle());
			mCtx.startService(myIntent);
			return reqCode;
		}

		alarmTimeVO.setReqCode(reqCode);

		//15분 이상일 경우 setTime 시킴
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


		ccc.add(Calendar.MINUTE, -14);
		boolean isSetAlarmClock = false;
		//15분 이내일 경우 바로 서비스 실행

		//Log.d(this.toString(), " calendar diff ccc=" +CommonUtils.convertFullDateType(ccc) + "  now = " + CommonUtils.convertFullDateType(nowCal));

		if(ccc.getTimeInMillis() <= nowCal.getTimeInMillis()){
			isSetAlarmClock = true;
			ccc.add(Calendar.MINUTE, 14);
			ccc.add(Calendar.SECOND, 55);
		}else{
			ccc.add(Calendar.MINUTE, 2);
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
	public void close(){
		mDb.closeDB();
	}
}
