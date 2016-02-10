package com.cyberocw.habittodosecretary.alaram;

import android.app.AlarmManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.alaram.db.AlarmDbManager;
import com.cyberocw.habittodosecretary.alaram.vo.TimerVO;

import java.util.ArrayList;

/**
 * Created by cyberocw on 2015-10-18.
 */
public class TimerDataManager {
	AlarmManager mManager;
	Context mCtx = null;
	AlarmDbManager mDb;
	ArrayList<TimerVO> dataList = null;

	public TimerDataManager(Context ctx) {
		mCtx = ctx;
		mDb = AlarmDbManager.getInstance(ctx);
		this.dataList = mDb.getTimerList();
	}

	public ArrayList<TimerVO> getDataList() {
		return dataList;
	}

	public void makeDataList(){
		this.dataList = mDb.getTimerList();
	}

	public void setDataList(ArrayList<TimerVO> dataList) {
		this.dataList = dataList;
	}

	public int getCount(){
		return this.dataList.size();
	}

	public TimerVO getItem(int position){
		return this.dataList.get(position);
	}

	public TimerVO getItemById(long id){
		for(int i = 0 ; i < dataList.size() ; i++){
			if(dataList.get(i).getId() == id){
				return dataList.get(i);
			}
		}
		return null;
	}

	public int getItemIndexById(long id){
		for(int i = 0 ; i < dataList.size() ; i++){
			if(dataList.get(i).getId() == id){
				return i;
			}
		}
		return -1;
	}

	public boolean deleteItemById(long id){
		boolean delResult = mDb.deleteTimer(id);

		if(delResult == false)
			return false;

		for(int i = 0 ; i < dataList.size() ; i++){
			if(dataList.get(i).getId() == id){
				dataList.remove(i);
				return true;
			}
		}
		return false;
	}

	public boolean addItem(TimerVO item){
		mDb.insertTimer(item);

		//알람 인던트 등록
		if(item.getId() == -1){
			Log.e(Const.DEBUG_TAG, "오류 : 알림 ID가 생성되지 않았습니다");
			Toast.makeText(mCtx, "오류 : 알림 ID가 생성되지 않았습니다", Toast.LENGTH_LONG).show();
			return false;
		}

		this.dataList.add(item);

		return true;
	}

	public boolean modifyItem(TimerVO item) {
		int nAffRow = mDb.updateTimer(item);

		if (nAffRow < 1){
			Toast.makeText(mCtx, "오류 : 수정에 실패했습니다.", Toast.LENGTH_LONG).show();
			return false;
		}
		else {
			int posi = this.getItemIndexById(item.getId());
			this.dataList.set(posi, item);
			return true;
		}
	}
}
