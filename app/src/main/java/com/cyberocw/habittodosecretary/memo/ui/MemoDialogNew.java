package com.cyberocw.habittodosecretary.memo.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Rating;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
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
import com.cyberocw.habittodosecretary.memo.vo.MemoVO;
import com.cyberocw.habittodosecretary.util.MyMovementMethod;

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
	Button mBtnSave;
	ImageButton mBtnAddAlarm;
	MemoVO mMemoVO;
	AlarmVO mAlarmVO;

	boolean isMemoEditable = true;
	boolean isModifyAlarm = false;
	long mCateId = -1;

	int mModifyMode = 0;

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
			mMemoVO = (MemoVO) arguments.getSerializable(Const.MEMO_VO);
			mAlarmVO = (AlarmVO) arguments.getSerializable(Const.ALARM_VO);

			if(mMemoVO != null)
				mModifyMode = 1;
			else
				mMemoVO = new MemoVO();
			mCateId = arguments.getLong(Const.CATEGORY.CATEGORY_ID);
			mMemoVO.setCategoryId(mCateId);
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
		mBtnAddAlarm = (ImageButton) mView.findViewById(R.id.btnAddAlarm);

		bindEvent();
		init();
	}

	private void init(){
		if(mModifyMode == 1){
			mTvTitle.setText(mMemoVO.getTitle());
			mEtMemoEditor.setText(mMemoVO.getContents());
			mTvMemoEditor.setText(mMemoVO.getContents());
			isMemoEditable = false;
			mRatingBar.setRating((float) mMemoVO.getRank());
		}
		bindEventSaveAndEdit();
	}

	private void bindEventSaveAndEdit(){
		if(isMemoEditable){
			mBtnSave.setText("SAVE");
			mTvMemoEditor.setVisibility(View.INVISIBLE);
			mEtMemoEditor.setVisibility(View.VISIBLE);

			mBtnSave.setOnClickListener(null);
			mBtnSave.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					returnData();
				}
			});
		}
		else{
			mBtnSave.setText("EDIT");

			mTvMemoEditor.setVisibility(View.VISIBLE);
			mEtMemoEditor.setVisibility(View.INVISIBLE);

			//mEtMemoEditor.setLinksClickable(true);
			mTvMemoEditor.setAutoLinkMask(Linkify.WEB_URLS);
			//mEtMemoEditor.setMovementMethod(MyMovementMethod.getInstance());
			//If the edit text contains previous text with potential links
			Linkify.addLinks(mTvMemoEditor, Linkify.WEB_URLS);

			//mEtMemoEditor.setClickable(false);
			mBtnSave.setOnClickListener(null);
			mBtnSave.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					isMemoEditable = true;
					bindEventSaveAndEdit();
				}
			});
		}
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


		mRatingBar.setIsIndicator(false);

		mBtnAddAlarm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				btnAddAlarmPopup();
			}
		});
	}

	private void btnAddAlarmPopup(){
		if(mAlarmVO == null) {
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
		if(mModifyMode == 1) {
			mAlarmVO.setId(-2);
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
		bundle.putSerializable(Const.MEMO_VO, mMemoVO);
		bundle.putSerializable(Const.ALARM_VO, mAlarmVO);
		alarmDialogNew.setArguments(bundle);
		alarmDialogNew.show(fm, "fragment_dialog_alarm_add");
		alarmDialogNew.setTargetFragment(this, Const.ALARM_INTERFACE_CODE.ADD_ALARM_CODE);
	}

	private void dataBind(){
		String title =  mTvTitle.getText().toString();
		mMemoVO.setTitle(title);
		mMemoVO.setContents(mEtMemoEditor.getText().toString());
		mMemoVO.setRank((int) mRatingBar.getRating());
	}

	private void returnData(){
		dataBind();

		Bundle bundle = new Bundle();
		bundle.putSerializable(Const.MEMO_VO, mMemoVO);

		if(isModifyAlarm && mAlarmVO != null)
			bundle.putSerializable(Const.ALARM_VO, mAlarmVO);

		Intent intent = new Intent();
		intent.putExtras(bundle);

		int returnCode = mModifyMode == 1 ? Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_MODIFY_FINISH_CODE : Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_FINISH_CODE;
		getTargetFragment().onActivityResult(getTargetRequestCode(), returnCode, intent);
		getActivity().getSupportFragmentManager().popBackStackImmediate();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		isModifyAlarm = true;
		mAlarmVO = (AlarmVO) data.getExtras().getSerializable("alarmVO");
		super.onActivityResult(requestCode, resultCode, data);
	}
}
