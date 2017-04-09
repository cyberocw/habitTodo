package com.cyberocw.habittodosecretary.memo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.util.Log;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.memo.db.MemoDbManager;
import com.cyberocw.habittodosecretary.memo.vo.MemoVO;

import java.util.ArrayList;

/**
 * Created by cyberocw on 2015-12-06.
 */
public class MemoDataManager {
	Context mCtx = null;
	MemoDbManager mDb;
	ArrayList<MemoVO> cachedDataList = null;
	ArrayList<MemoVO> dataList = null;

	public MemoDataManager(Context ctx) {
		mCtx = ctx;
		mDb = MemoDbManager.getInstance(ctx);
		//makeDataList();
	}

	public MemoDataManager(Context ctx, Long cateId) {
		mCtx = ctx;
		mDb = MemoDbManager.getInstance(ctx);
		SharedPreferences prefs = mCtx.getSharedPreferences(Const.ALARM_SERVICE_ID, Context.MODE_PRIVATE);
		String sortOption = prefs.getString(Const.MEMO.SORT_KEY, Const.MEMO.SORT_REG_DATE_DESC);
		makeDataList(cateId, sortOption);
	}

	public ArrayList<MemoVO> getDataList() {
		return dataList;
	}

	public void makeDataList(){
		this.dataList = mDb.getList();
	}

	public void makeDataList(Long cateId, String sortOption){
		dataList = mDb.getListByCate(cateId, sortOption);
		cachedDataList = (ArrayList) dataList.clone();
	}

	public void setDataList(ArrayList<MemoVO> dataList) {
		this.dataList = dataList;
	}

	public int getCount(){
		return this.dataList.size();
	}

	public MemoVO getItem(int position){
		return this.dataList.get(position);
	}

	public MemoVO getItemById(long id){
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
		boolean delResult = mDb.delete(id);

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

	public boolean addItem(MemoVO item){
		mDb.insert(item);

		//알람 인던트 등록
		if(item.getId() == -1){
			Toast.makeText(mCtx, "오류 : CATEGORY ID가 생성되지 않았습니다", Toast.LENGTH_LONG).show();
			return false;
		}

		this.dataList.add(item);

		return true;
	}

	public boolean modifyItem(MemoVO item) {
		int nAffRow = mDb.update(item);

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

	public void filter(String text){
		ArrayList<MemoVO> newDataList = new ArrayList<>();
		for(int i = 0 ; i < this.cachedDataList.size(); i++){
			if (cachedDataList.get(i).getTitle().contains(text) || cachedDataList.get(i).getContents().contains(text)) {
				newDataList.add(cachedDataList.get(i));
			}
		}
		this.dataList = newDataList;
	}

	public void resetFilter() {
		this.dataList = cachedDataList;
	}
}
