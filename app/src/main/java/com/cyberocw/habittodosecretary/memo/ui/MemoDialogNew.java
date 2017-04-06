package com.cyberocw.habittodosecretary.memo.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.ui.AlarmDialogNew;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.category.CategoryDataManager;
import com.cyberocw.habittodosecretary.category.CategoryListAdapter;
import com.cyberocw.habittodosecretary.category.vo.CategoryVO;
import com.cyberocw.habittodosecretary.memo.vo.MemoVO;
import com.cyberocw.habittodosecretary.util.CommonUtils;

import java.util.ArrayList;

/**
 * Created by cyberocw on 2015-12-14.
 */
public class MemoDialogNew extends Fragment{
	View mView;
	Context mCtx;
	EditText mTvTitle;
	Spinner mSpCategory;
	EditText mEtMemoEditor;
	TextView mTvMemoEditor;
	RatingBar mRatingBar;
	Button mBtnSave, mBtnEdit;
	ImageButton mBtnAddAlarm;
	MemoVO mMemoVO;
	AlarmVO mAlarmVO, mAlarmOriginalVO = null;
	CategoryDataManager mCateDataManager;
	CategoryListAdapter mCateAdapter;
	ArrayList<CategoryVO> mArrayCategoryVOList = null;
	boolean mShareMode = false;

	long mSelectedCateId = -1;
	boolean isMemoEditable = true;
	boolean isModifyAlarm = false;
	int mModifyMode = 0;
	long mInitAlarmId = -1;

	public MemoDialogNew() {
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {

		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		mView = inflater.inflate(R.layout.fragment_dialog_memo, container, false);
		Toolbar toolbar = (Toolbar) mView.findViewById(R.id.toolbar);
		toolbar.setVisibility(View.VISIBLE);
		toolbar.setTitle(R.string.app_name);
		toolbar.setNavigationIcon(R.drawable.ic_arrow_left);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().onBackPressed();
			}
		});
		return mView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		mCtx = getActivity();
		Bundle arguments = getArguments();

		if(arguments != null) {
			mMemoVO = (MemoVO) arguments.getSerializable(Const.PARAM.MEMO_VO);
			mAlarmVO = (AlarmVO) arguments.getSerializable(Const.PARAM.ALARM_VO);
			if(mAlarmVO != null){
				mInitAlarmId = mAlarmVO.getId();
				mAlarmOriginalVO = mAlarmVO;
			}
			mSelectedCateId = (long) arguments.getSerializable(Const.CATEGORY.CATEGORY_ID);

			if(arguments.containsKey(Const.MEMO.MEMO_INTERFACE_CODE.SHARE_MEMO_MODE))
				mShareMode = (boolean) arguments.getSerializable(Const.MEMO.MEMO_INTERFACE_CODE.SHARE_MEMO_MODE);

			if(mShareMode == true){

			}
			else if(mMemoVO != null)
				mModifyMode = 1;
			else
				mMemoVO = new MemoVO();
		}
		else{
			Toast.makeText(mCtx, "카테고리 ID가 전달되지 않았습니다", Toast.LENGTH_SHORT).show();
			getFragmentManager().popBackStackImmediate();
		}

		initActivity();

		//mPrefs = mCtx.getSharedPreferences(Const.ALARM_SERVICE_ID, mCtx.MODE_PRIVATE);

		/*
		mActionBar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_action_back));
		mActionBar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//What to do on back clicked
			}
		});
		*/

	}
	public void initActivity(){
		mTvTitle = (EditText) mView.findViewById(R.id.txMemoTitle);
		mSpCategory = (Spinner) mView.findViewById(R.id.spCategory);
		mEtMemoEditor = (EditText) mView.findViewById(R.id.etMemoEditor);
		mTvMemoEditor = (TextView) mView.findViewById(R.id.tvMemoEditor);
		mRatingBar = (RatingBar) mView.findViewById(R.id.ratingBar);
		mBtnSave = (Button) mView.findViewById(R.id.btnMemoSave);
		mBtnEdit = (Button) mView.findViewById(R.id.btnEdit);
		mBtnAddAlarm = (ImageButton) mView.findViewById(R.id.btnAddAlarm);
		makeCategoryList();
		bindEvent();
		init();
	}

	private void init(){
		if(mModifyMode == 1 || mShareMode){
			mTvTitle.setText(mMemoVO.getTitle());
			mEtMemoEditor.setText(mMemoVO.getContents());
			mTvMemoEditor.setText(mMemoVO.getContents());
			isMemoEditable = false;
			mRatingBar.setRating((float) mMemoVO.getRank());
			mSelectedCateId = mMemoVO.getCategoryId();
		}

		if(mSelectedCateId != -1){
			for(int i = 0; i < mArrayCategoryVOList.size(); i++){
				if(mArrayCategoryVOList.get(i).getId() == mSelectedCateId){
					mSpCategory.setSelection(i);
					break;
				}
			}
		}

		bindEventSaveAndEdit();
	}

	private void makeCategoryList(){
		ArrayList<String> arrayList = new ArrayList<String>();
		mCateDataManager = new CategoryDataManager(mCtx);
		//mCateAdapter = new CategoryListAdapter(this, mCtx, mCateDataManager);
		mArrayCategoryVOList = mCateDataManager.getDataList();

		for(int i = 0; i < mArrayCategoryVOList.size(); i++){
			arrayList.add(mArrayCategoryVOList.get(i).getTitle());
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, arrayList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		//스피너 속성
		mSpCategory.setPrompt("카테고리"); // 스피너 제목
		mSpCategory.setAdapter(adapter);
		mSpCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				//if(position > 0)
					mSelectedCateId = mArrayCategoryVOList.get(position).getId();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	private void bindEventSaveAndEdit(){
		if(mMemoVO.getId() == -1){
			mBtnEdit.setVisibility(View.GONE);
		}
		else if(isMemoEditable){
			mBtnEdit.setText("완료");

			mTvMemoEditor.setVisibility(View.GONE);
			mEtMemoEditor.setVisibility(View.VISIBLE);
			//mTvMemoEditor.setOnClickListener(null);
			//mBtnEdit.setOnClickListener(null);

		}
		else{
			mBtnEdit.setText(getResources().getText(R.string.btn_memo_modify));
			mTvMemoEditor.setText(mEtMemoEditor.getText());
			mTvMemoEditor.setVisibility(View.VISIBLE);
			mEtMemoEditor.setVisibility(View.GONE);

			//mEtMemoEditor.setLinksClickable(true);
			mTvMemoEditor.setAutoLinkMask(Linkify.WEB_URLS);
			//mEtMemoEditor.setMovementMethod(MyMovementMethod.getInstance());
			//If the edit text contains previous text with potential links
			Linkify.addLinks(mTvMemoEditor, Linkify.WEB_URLS);
		}

		CommonUtils.setupUI(mView, getActivity());
	}

	private void bindEvent(){
		/*
		mBtnSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				returnData();
			}
		});
		*/

		mEtMemoEditor.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				//Linkify.addLinks(s, Linkify.WEB_URLS);
			}
		});

		mBtnSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				returnData();
			}
		});

		mRatingBar.setIsIndicator(false);

		mBtnAddAlarm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				btnAddAlarmPopup();
			}
		});

		mBtnEdit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//returnData();
				isMemoEditable = !isMemoEditable;
				bindEventSaveAndEdit();
			}
		});

		mTvMemoEditor.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isMemoEditable = !isMemoEditable;
				bindEventSaveAndEdit();
			}
		});
	}

	private void btnAddAlarmPopup(){
		if(mAlarmVO == null || mAlarmVO.getId() == -2) {
			showAlarmPopup();
		}else{
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
							showAlarmPopup();
							break;
						case 1:
							deleteAlarm();
							break;
					}
					dialogInterface.dismiss();
				}
			});
		}
	}

	private void deleteAlarm(){
		if(mModifyMode == 1 && mAlarmVO.getId() > -1) {
			mAlarmVO.setId(-2);
			//mInitAlarmId = -1;
		}
		else {
			mAlarmVO = null;
		}
		isModifyAlarm = true;
	}

	private void showAlarmPopup(){
		FragmentManager fm;
		fm = getFragmentManager();

		AlarmDialogNew alarmDialogNew = new AlarmDialogNew();
		Bundle bundle = new Bundle();
		dataBind();
		bundle.putSerializable(Const.PARAM.MEMO_VO, mMemoVO);

		if(mAlarmVO != null && mAlarmVO.getId() > -2)
		bundle.putSerializable(Const.PARAM.ALARM_VO, mAlarmVO);

		bundle.putBoolean(Const.MEMO.IS_INIT_MEMO_MODE, true);

		alarmDialogNew.setArguments(bundle);
		//alarmDialogNew.show(fm, "fragment_dialog_alarm_add");

		FragmentTransaction transaction = fm.beginTransaction();
		// For a little polish, specify a transition animation
		transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
		// To make it fullscreen, use the 'content' root view as the container
		// for the fragment, which is always the root view for the activity
		alarmDialogNew.setTargetFragment(this, Const.ALARM_INTERFACE_CODE.ADD_ALARM_CODE);

		transaction.add(R.id.warpContainer, alarmDialogNew, "fragment_dialog_alarm_add")
				.addToBackStack(null).commit();


	}

	private void dataBind(){
		String title =  mTvTitle.getText().toString();
		mMemoVO.setCategoryId(mSelectedCateId);
		mMemoVO.setTitle(title);
		mMemoVO.setContents(mEtMemoEditor.getText().toString());
		mMemoVO.setRank((int) mRatingBar.getRating());
	}

	private boolean validate(){
		if(mSelectedCateId == -1){
			Toast.makeText(mCtx, "카테고리를 선택해 주세요", Toast.LENGTH_SHORT).show();
			return false;
		}

		if(mTvTitle.getText().equals("")){
			Toast.makeText(mCtx, "제목을 입력해 주세요", Toast.LENGTH_SHORT).show();
			return false;
		}

		return true;
	}

	private void returnData(){
		dataBind();
		if(validate() == false){
			return ;
		}
		Bundle bundle = new Bundle();
		bundle.putSerializable(Const.PARAM.MEMO_VO, mMemoVO);

		if(mInitAlarmId > -1){
			bundle.putLong(Const.MEMO.ORIGINAL_ALARM_ID_KEY, mInitAlarmId);
		}

		if(isModifyAlarm && mAlarmVO != null)
			bundle.putSerializable(Const.PARAM.ALARM_VO, mAlarmVO);

		Intent intent = new Intent();
		intent.putExtras(bundle);

		int returnCode = mModifyMode == 1 ? Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_MODIFY_FINISH_CODE : Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_FINISH_CODE;

		getTargetFragment().onActivityResult(getTargetRequestCode(), returnCode, intent);
		getActivity().getSupportFragmentManager().popBackStackImmediate();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		isModifyAlarm = true;
		mAlarmVO = (AlarmVO) data.getExtras().getSerializable(Const.PARAM.ALARM_VO);
		super.onActivityResult(requestCode, resultCode, data);
	}
}
