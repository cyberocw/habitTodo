package com.cyberocw.habittodosecretary.category;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.category.db.CategoryDbManager;
import com.cyberocw.habittodosecretary.category.vo.CategoryVO;

import java.util.ArrayList;

/**
 * Created by cyberocw on 2015-12-06.
 */
public class CategoryDataManager {
	Context mCtx = null;
	CategoryDbManager mDb;
	ArrayList<CategoryVO> dataList = null;

	public CategoryDataManager(Context ctx) {
		mCtx = ctx;
		mDb = CategoryDbManager.getInstance(ctx);
		makeDataList();
	}

	public ArrayList<CategoryVO> getDataList() {
		return dataList;
	}

	public void makeDataList(){
		this.dataList = mDb.getCategoryList();
	}

	public void setDataList(ArrayList<CategoryVO> dataList) {
		this.dataList = dataList;
	}

	public int getCount(){
		return this.dataList.size();
	}

	public CategoryVO getItem(int position){
		return this.dataList.get(position);
	}

	public CategoryVO getItemById(long id){
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
		boolean delResult = mDb.deleteCategory(id);

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

	public boolean addItem(CategoryVO item){
		mDb.insertCategory(item);

		//알람 인던트 등록
		if(item.getId() == -1){
			Log.e(Const.DEBUG_TAG, "오류 : CATEGORY ID가 생성되지 않았습니다");
			return false;
		}

		this.dataList.add(item);

		return true;
	}

	public boolean modifyItem(CategoryVO item) {
		int nAffRow = mDb.updateCategory(item);

		if (nAffRow < 1){
			return false;
		}
		else {
			int posi = this.getItemIndexById(item.getId());
			this.dataList.set(posi, item);
			return true;
		}
	}
}
