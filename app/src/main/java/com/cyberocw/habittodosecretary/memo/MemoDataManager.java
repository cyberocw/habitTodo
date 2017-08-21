package com.cyberocw.habittodosecretary.memo;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.common.vo.FileVO;
import com.cyberocw.habittodosecretary.file.FileDataManager;
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
	String mSortOption = "";
	long mCateId = -1;
    int mCnt = 0;
	FileDataManager mFileDataManager = null;

	public MemoDataManager(Context ctx) {
		mCtx = ctx;
		mDb = MemoDbManager.getInstance(ctx);
		//makeDataList();
	}

	public MemoDataManager(Context ctx, Long cateId) {
		mCtx = ctx;
		mDb = MemoDbManager.getInstance(ctx);
		mCateId = cateId;
		SharedPreferences prefs = mCtx.getSharedPreferences(Const.ALARM_SERVICE_ID, Context.MODE_PRIVATE);
		mSortOption = prefs.getString(Const.MEMO.SORT_KEY, Const.MEMO.SORT_REG_DATE_DESC);
		makeDataList(mCateId, mSortOption);
	}

	public MemoDataManager(Context ctx, Long cateId, int cnt) {
		mCtx = ctx;
		mDb = MemoDbManager.getInstance(ctx);
		mCateId = cateId;
        mCnt = cnt;
		SharedPreferences prefs = mCtx.getSharedPreferences(Const.ALARM_SERVICE_ID, Context.MODE_PRIVATE);
		mSortOption = prefs.getString(Const.MEMO.SORT_KEY, Const.MEMO.SORT_REG_DATE_DESC);
		makeDataList(mCateId, mSortOption, mCnt);
	}

	public void refreshData(){
		makeDataList(mCateId, mSortOption);
	}

	public ArrayList<MemoVO> getDataList() {
		return dataList;
	}

	public void makeDataList(){
		this.dataList = mDb.getList();
	}
	public void makeDataList(int cnt){
		this.dataList = mDb.getList(cnt);
	}

	public void makeDataList(Long cateId, String sortOption, int cnt){
		dataList = mDb.getListByCate(cateId, sortOption, cnt);
		FileDataManager fdm = new FileDataManager(mCtx);
		for(int i = 0 ; i < dataList.size(); i++){
			fdm.makeDataList(Const.ETC_TYPE.MEMO, dataList.get(i).getId());
			dataList.get(i).setFileList(fdm.getDataList());
		}
		cachedDataList = (ArrayList) dataList.clone();
	}

	public void makeDataList(Long cateId, String sortOption){
		this.makeDataList(cateId, sortOption, 0);
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

		MemoVO vo = this.getItemById(id);
		mFileDataManager = new FileDataManager(mCtx);
		mFileDataManager.addDeleteItem(vo.getFileList());
		mFileDataManager.addDeleteItem(vo.getDelFileList());
		mFileDataManager.deleteAll();
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
			Toast.makeText(mCtx, mCtx.getString(R.string.db_failed_generate_id), Toast.LENGTH_LONG).show();
			return false;
		}

		this.dataList.add(item);

		return true;
	}

	public boolean modifyItem(MemoVO item) {
		int nAffRow = mDb.update(item);
		if (nAffRow < 1){
			Toast.makeText(mCtx, mCtx.getString(R.string.msg_failed_modify), Toast.LENGTH_LONG).show();
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
