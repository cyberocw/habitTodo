package com.cyberocw.habittodosecretary.memo;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.MainActivity;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.AlarmDataManager;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.common.vo.FileVO;
import com.cyberocw.habittodosecretary.common.vo.RelationVO;
import com.cyberocw.habittodosecretary.db.CommonRelationDBManager;
import com.cyberocw.habittodosecretary.file.FileDataManager;
import com.cyberocw.habittodosecretary.file.StorageHelper;
import com.cyberocw.habittodosecretary.memo.ui.MemoDialogNew;
import com.cyberocw.habittodosecretary.memo.vo.MemoVO;
import com.cyberocw.habittodosecretary.record.RecorderDataManager;
import com.cyberocw.habittodosecretary.util.CommonUtils;
import com.cyberocw.habittodosecretary.util.PopMessageEvent;
import com.cyberocw.habittodosecretary.util.TitleMessageEvent;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MemoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MemoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemoFragment extends Fragment {
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	MemoDataManager mMemoDataManager;
	MemoListAdapter mMemoAdapter;
	AlarmDataManager mAlarmDataManager;
	RecorderDataManager mRecorderDataManager;
	FileDataManager mFileDataManager;
	private CommonRelationDBManager mCommonRelationDBManager;

	boolean mIsShareMode = false;
	boolean mIsEtcMode = false;
	boolean mIsEtcViewMode = false;

	private View mView;
	ListView mListView;
	private EditText mEtSearchKeyword;
	private Button btnSortMemo;
	private Context mCtx;
	SharedPreferences mPrefs;
	private long mCateId = -1, mMemoId = -1;
	FloatingActionsMenu mFab;

	private OnFragmentInteractionListener mListener;

	public MemoFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//MainActivity.pushActionBarInfo(R.string.nav_item_memo, false);
		EventBus.getDefault().post(new TitleMessageEvent(getString(R.string.nav_item_memo), false));
		if (getArguments() != null) {
			Bundle arguments = getArguments();

			if(arguments.containsKey(Const.CATEGORY.CATEGORY_ID))
				mCateId = arguments.getLong(Const.CATEGORY.CATEGORY_ID);
			if(arguments.containsKey(Const.MEMO.MEMO_INTERFACE_CODE.SHARE_MEMO_MODE))
				mIsShareMode = (boolean) arguments.get(Const.MEMO.MEMO_INTERFACE_CODE.SHARE_MEMO_MODE);
			if(arguments.containsKey(Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_ETC_KEY)){
				mIsEtcMode = arguments.getBoolean(Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_ETC_KEY);
			}
			if(arguments.containsKey(Const.PARAM.ETC_TYPE_KEY) || arguments.containsKey(Const.MEMO.MEMO_INTERFACE_CODE.VIEW_MEMO_ETC_KEY)){
				mIsEtcViewMode = true;
			}

		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		mView = inflater.inflate(R.layout.fragment_memo, container, false);

		Bundle args = getArguments();
		boolean showToolbar = false;
		if(args != null){
			//showToolbar = args.getBoolean(Const.MEMO.SHOW_TOOLBAR, false);
		}

		if(showToolbar) {
			Toolbar toolbar = (Toolbar) mView.findViewById(R.id.toolbar);
			toolbar.setVisibility(View.VISIBLE);
			toolbar.setTitle(R.string.app_name);
			toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
			toolbar.setNavigationOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					getActivity().onBackPressed();
				}
			});
			if(args.containsKey(Const.CATEGORY.CATEGORY_TITLE_KEY))
				toolbar.setTitle(args.getString(Const.CATEGORY.CATEGORY_TITLE_KEY));
		}

		mEtSearchKeyword = ButterKnife.findById(mView, R.id.etSearchKeyword);
		btnSortMemo = ButterKnife.findById(mView, R.id.btnSortMemo);
		mFab = ButterKnife.findById(mView, R.id.multiple_actions);
		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		mCtx = getActivity();
		mPrefs = mCtx.getSharedPreferences(Const.ALARM_SERVICE_ID, Context.MODE_PRIVATE);

		/*
		mActionBar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_action_back));
		mActionBar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//What to do on back clicked
			}
		});
		*/
		initActivity();
	}


	private void initActivity(){
		mMemoDataManager = new MemoDataManager(mCtx, mCateId);
		mMemoAdapter = new MemoListAdapter(this, mCtx, mMemoDataManager);
		mAlarmDataManager = new AlarmDataManager(mCtx);

		mListView = (ListView) mView.findViewById(R.id.memoListView);
		mListView.setAdapter(mMemoAdapter);
		mListView.setOnItemClickListener(new ListViewItemClickListener());

		mCommonRelationDBManager = CommonRelationDBManager.getInstance(mCtx);

		bindEvent();

		// 각 보드별 UI 구성
		if(mIsShareMode){
			showNewMemoDialog();
		}
		else if(mIsEtcViewMode){
			long alarmId = getArguments().getLong(Const.PARAM.ALARM_ID);
			RelationVO rVO = mCommonRelationDBManager.getByAlarmId(alarmId);
			if(rVO.getfId() == -1){
				Toast.makeText(mCtx, getString(R.string.fragment_memo_no_relation), Toast.LENGTH_LONG).show();
				Log.e(this.toString(), "error! Relation 정보를 가져올 수 없습니다");
			}
			else
				showNewMemoDialog(rVO.getfId());
		}

		CommonUtils.logCustomEvent("MemoFragment", "1", "memo Count", mMemoDataManager.getCount());
	}



	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	private void bindEvent(){
		FloatingActionButton fabMemo = (FloatingActionButton) mView.findViewById(R.id.fabAddMemo);
		FloatingActionButton fabTodo = ButterKnife.findById(mView, R.id.fabAddTodo);

		fabMemo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showNewMemoDialog();
			}
		});
		fabTodo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showNewMemoDialog(true);
			}
		});
		mEtSearchKeyword.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				String text = mEtSearchKeyword.getText().toString()
						.toLowerCase(Locale.getDefault());
				mMemoAdapter.filter(text);

			}
		});
		btnSortMemo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String names[] ={getString(R.string.memo_sort_edit_decending),getString(R.string.memo_sort_edit_ascending), getString(R.string.memo_sort_importance_decending), getString(R.string.memo_sort_importance_ascending)};
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(mCtx);

				ListView lv = new ListView(mCtx);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				alertDialog.setView(lv);
				alertDialog.setTitle(getString(R.string.dialog_memo_sort_title));

				lv.setLayoutParams(params);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(mCtx,android.R.layout.simple_list_item_1,names);
				lv.setAdapter(adapter);

				final DialogInterface dialogInterface = alertDialog.show();



				lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						String sortOption = Const.MEMO.SORT_REG_DATE_DESC;
						switch (position) {
							case 0:
								sortOption = Const.MEMO.SORT_REG_DATE_DESC;
								break;
							case 1:
								sortOption = Const.MEMO.SORT_REG_DATE_ASC;
								break;
							case 2:
								sortOption = Const.MEMO.SORT_STAR_DESC;
								break;
							case 3:
								sortOption = Const.MEMO.SORT_STAR_ASC;
								break;
						}
						SharedPreferences.Editor editor = mPrefs.edit();
						editor.remove(Const.MEMO.SORT_KEY);
						editor.putString(Const.MEMO.SORT_KEY, sortOption);
						editor.commit();
						mMemoDataManager.makeDataList(mCateId, sortOption);
						mMemoAdapter.notifyDataSetChanged();
						mListView.setSelection(0);
						dialogInterface.dismiss();
					}
				});
			}
		});

		CommonUtils.setupUI(mView, getActivity());
	}

	private class ListViewItemClickListener implements AdapterView.OnItemClickListener
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			if(mIsEtcMode == false)
				showNewMemoDialog(mMemoDataManager.getItem(position).getId());
			else{
				// 알람화면에서 메모를 등록하는 방식일때 사용
				/*
				MemoVO memoVO = mMemoDataManager.getItem(position);
				if(memoVO.getAlarmId() > -1){
					Toast.makeText(mCtx, getString(R.string.fragment_memo_already_alarm), Toast.LENGTH_LONG).show();
					return;
				}

				Bundle bundle = new Bundle();
				bundle.putSerializable(Const.PARAM.MEMO_VO, memoVO);
				Intent intent = new Intent();
				intent.putExtras(bundle);

				//int returnCode = mModifyMode == 1 ? Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_MODIFY_FINISH_CODE : Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_FINISH_CODE;

				getActivity().getSupportFragmentManager().popBackStackImmediate();
				getTargetFragment().onActivityResult(getTargetRequestCode(), Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_ETC_CODE, intent);*/
			}

		}
	}



	public void showNewMemoDialog(){
		showNewMemoDialog(-1);
	}
	public void showNewMemoDialog(boolean b){
		showNewMemoDialog(-1, b);
	}
	public void showNewMemoDialog(long id) {
		this.showNewMemoDialog(id, false);
	}
	public void showNewMemoDialog(long id, boolean isTodo) {
		if(mFab != null)
			mFab.collapse();

		MemoDialogNew dialogNew = new MemoDialogNew();
		Bundle bundle = new Bundle();

		if(id != -1) {
			// relation이 있으면 가져옴
			RelationVO relationVO = mCommonRelationDBManager.getByTypeId(Const.ETC_TYPE.MEMO, id);

			Crashlytics.log(Log.DEBUG, Const.DEBUG_TAG, " relationVO = " + relationVO != null ? relationVO.toString() : "null");

			if(relationVO != null && relationVO.getAlarmId() != -1) {
				AlarmVO alarmVO = mAlarmDataManager.getItemByIdInDB(relationVO.getAlarmId());
				if(alarmVO != null) {
					if(alarmVO.getAlarmOption() == Const.ALARM_OPTION_TO_SOUND.RECORD) {
						getFileDataManager().makeDataList(Const.ETC_TYPE.ALARM, alarmVO.getId());
						alarmVO.setFileList(getFileDataManager().getDataList());
					}

					bundle.putSerializable(Const.PARAM.ALARM_VO, alarmVO);
				}
			}

			bundle.putSerializable(Const.PARAM.MEMO_VO, mMemoDataManager.getItemById(id));
		}
		else if(mIsShareMode){
			bundle.putSerializable(Const.PARAM.MEMO_VO, (MemoVO) getArguments().get(Const.PARAM.MEMO_VO));
			bundle.putSerializable(Const.MEMO.MEMO_INTERFACE_CODE.SHARE_MEMO_MODE, mIsShareMode);
		}

		if(isTodo){
			bundle.putBoolean(Const.PARAM.IS_TODO, true);
		}

		bundle.putSerializable(Const.CATEGORY.CATEGORY_ID, mCateId);

		dialogNew.setArguments(bundle);
		FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.add(R.id.warpContainer, dialogNew, Const.FRAGMENT_TAG.MEMO_DIALOG);
		ft.addToBackStack(null).commit();
		dialogNew.setTargetFragment(this, Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_CODE);
	}



	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		MemoVO memoVO = null;
		AlarmVO alarmVO = null;
		Bundle bundle = data.getExtras();
		if(bundle == null)
			return ;
		if(bundle.containsKey(Const.PARAM.MEMO_VO))
			memoVO = (MemoVO) bundle.getSerializable(Const.PARAM.MEMO_VO);
		if(bundle.containsKey(Const.PARAM.ALARM_VO))
			alarmVO = (AlarmVO) bundle.getSerializable(Const.PARAM.ALARM_VO);


		switch(resultCode) {
			case Const.MEMO.MEMO_INTERFACE_CODE.DEL_MEMO_FINISH_CODE :
				if(memoVO != null)
					deleteMemo(memoVO.getId());
				break;
			case Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_FINISH_CODE :
				// 메모 추가
				if(mMemoDataManager.addItem(memoVO) == true)
					mMemoAdapter.notifyDataSetChanged();
				else
					Toast.makeText(mCtx, getString(R.string.msg_failed_insert), Toast.LENGTH_LONG).show();

				// main Activity 사용 또는 인스턴스 생성
				// 알람 추가
				if(alarmVO != null) {
					if (mAlarmDataManager.addItem(alarmVO) == true) {
						if (!insertRelation(alarmVO, memoVO)){
							Toast.makeText(mCtx, getString(R.string.msg_failed_relation_insert_db), Toast.LENGTH_LONG).show();
						}
						else if (alarmVO.getAlarmOption() == Const.ALARM_OPTION_TO_SOUND.RECORD) {
							String fromPath = bundle.getString(Const.PARAM.FILE_PATH);
							if (fromPath == null) {
								Toast.makeText(mCtx, "음성 파일이 저장 되지 않았습니다", Toast.LENGTH_SHORT).show();
								return;
							}
							//신규 파일 만들고 복사하고 기존파일 삭제
							File targetFile = StorageHelper.createNewAttachmentFile(mCtx, Environment.DIRECTORY_RINGTONES, ".wav");
							boolean result = getRecorderDataManager().saveFile(fromPath, targetFile);
							if (result) {
								Log.d(this.toString(), "미디어 복사 성공");
								getRecorderDataManager().deleteRecordFile(fromPath);
								//db저장
								mAlarmDataManager.saveFile(alarmVO, targetFile);
							}
						}
						mAlarmDataManager.resetMinAlarmCall();
						if(alarmVO.getAlarmReminderType() == Const.ALARM_REMINDER_MODE.REMINDER)
							mAlarmDataManager.resetReminderNoti();
					}
					else {
						Toast.makeText(mCtx, getString(R.string.msg_failed_insert), Toast.LENGTH_LONG).show();
					}
				}
				Toast.makeText(mCtx, getString(R.string.success), Toast.LENGTH_SHORT).show();
				break;
			case Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_MODIFY_FINISH_CODE :
				if(mMemoDataManager.modifyItem(memoVO) == true) {
					mMemoAdapter.notifyDataSetChanged();
				}
				else {
					Toast.makeText(mCtx, getString(R.string.msg_failed_modify), Toast.LENGTH_LONG).show();
					break;
				}
				//새롭게 등록할 알림이 있는경우
				if(alarmVO != null) {
					//신규 알림 추가
					if(alarmVO.getId() == -1){
						if (mAlarmDataManager.addItem(alarmVO) == false) {
							Toast.makeText(mCtx, getString(R.string.msg_failed_insert), Toast.LENGTH_LONG).show();
							break;
						}else {
							//기존것 제거하고서 신규 추가한 경우 기존것 지움
							if(bundle.containsKey(Const.MEMO.ORIGINAL_ALARM_ID_KEY)) {
								AlarmVO oriVO = mAlarmDataManager.getItemByIdInList(bundle.getLong(Const.MEMO.ORIGINAL_ALARM_ID_KEY));

								mAlarmDataManager.deleteItemById(bundle.getLong(Const.MEMO.ORIGINAL_ALARM_ID_KEY));
								if(oriVO.getAlarmOption() == Const.ALARM_OPTION_TO_SOUND.RECORD){
									mAlarmDataManager.deleteItemFileDbReal(oriVO);
								}
							}
							insertRelation(alarmVO, memoVO);

							if (alarmVO.getAlarmOption() == Const.ALARM_OPTION_TO_SOUND.RECORD){
								String fromPath = bundle.getString(Const.PARAM.FILE_PATH);
								if(fromPath == null){
									Toast.makeText(mCtx, "음성 파일이 저장 되지 않았습니다", Toast.LENGTH_SHORT).show();
									return ;
								}
								//신규 파일 만들고 복사하고 기존파일 삭제
								File targetFile = StorageHelper.createNewAttachmentFile(mCtx, Environment.DIRECTORY_RINGTONES, ".wav");
								boolean result = getRecorderDataManager().saveFile(fromPath, targetFile);
								if(result){
									Log.d(this.toString(), "미디어 복사 성공");
									getRecorderDataManager().deleteRecordFile(fromPath);
									//db저장
									mAlarmDataManager.saveFile(alarmVO, targetFile);
								}
							}
						}
					}
					//기존 알림을 수정했을 경우. (알림 수정화면 다녀옴)
					else{
						//alarm modify시 delete 후 insert 과정에서 delete하면서 relation도 함께 지워짐
						if (mAlarmDataManager.modifyItem(alarmVO) == false) {
							Toast.makeText(mCtx, getString(R.string.msg_failed_modify), Toast.LENGTH_LONG).show();
							break;
						}
						else{
							String fromPath = bundle.getString(Const.PARAM.FILE_PATH, null);
							if (alarmVO.getAlarmOption() == Const.ALARM_OPTION_TO_SOUND.RECORD){
								//파일 잘 들어오는지 확인 필요
								ArrayList<FileVO> oriArrFile = alarmVO.getFileList();
								alarmVO.setFileList(null);

								//String oriPath = CommonUtils.getRecordFullPath(mCtx, oriAlarmId);
								if(fromPath == null){
									Toast.makeText(mCtx, "오류! 음성 파일이 저장 되지 않았습니다", Toast.LENGTH_SHORT).show();
									return ;
								}
								File targetFile = StorageHelper.createNewAttachmentFile(mCtx, Environment.DIRECTORY_RINGTONES, ".wav");
								//파일 복사해두고 기존 데이터 및 파일 제거 후, 파일 정보 db 등록
								boolean result = getRecorderDataManager().saveFile(fromPath, targetFile);
								if(result){
									Log.d(this.toString(), "미디어 복사 성공");
									if(oriArrFile != null && oriArrFile.size() > 0) {
										getFileDataManager().addDeleteItem(oriArrFile);
										getFileDataManager().deleteAll(Environment.DIRECTORY_RINGTONES);
									}
									getRecorderDataManager().deleteRecordFile(fromPath);
									mAlarmDataManager.saveFile(alarmVO, targetFile);
								}
							}
							else if(fromPath != null){
								ArrayList<FileVO> oriArrFile = alarmVO.getFileList();
                                if(oriArrFile != null && oriArrFile.size() > 0) {
                                    //getRecorderDataManager().deleteRecordFile(fromPath);
                                    getFileDataManager().addDeleteItem(oriArrFile);
                                    getFileDataManager().deleteAll(Environment.DIRECTORY_RINGTONES);
                                    alarmVO.setFileList(null);
                                }
							}
						}
					}
					mAlarmDataManager.resetMinAlarmCall();
					if(alarmVO.getAlarmReminderType() == Const.ALARM_REMINDER_MODE.REMINDER)
						mAlarmDataManager.resetReminderNoti();
				}
				//기존 알람이 있는데, 알람을 신규 드록했다가 다시 제거했을 경우
				else if(bundle.containsKey(Const.MEMO.ORIGINAL_ALARM_ID_KEY)){
					AlarmVO oriVO = mAlarmDataManager.getItemByIdInList(bundle.getLong(Const.MEMO.ORIGINAL_ALARM_ID_KEY));
					mAlarmDataManager.deleteItemById(bundle.getLong(Const.MEMO.ORIGINAL_ALARM_ID_KEY));
					if(oriVO.getAlarmOption() == Const.ALARM_OPTION_TO_SOUND.RECORD){
						mAlarmDataManager.deleteItemFileDbReal(oriVO);
					}
					mAlarmDataManager.resetMinAlarmCall();
					if(oriVO.getAlarmReminderType() == Const.ALARM_REMINDER_MODE.REMINDER)
						mAlarmDataManager.resetReminderNoti();
				}
				Toast.makeText(getActivity(), getString(R.string.success), Toast.LENGTH_SHORT).show();
				break;
		}


		if(mIsShareMode){
			Toast.makeText(mCtx, getString(R.string.success), Toast.LENGTH_SHORT).show();
			getActivity().finish();
		}else{
			mListView.removeAllViewsInLayout();
			mMemoDataManager.refreshData();
			mMemoAdapter.notifyDataSetChanged();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public FileDataManager getFileDataManager(){
		if(mFileDataManager == null){
			mFileDataManager = new FileDataManager(mCtx);
		}
		return mFileDataManager;
	}

	public void deleteItemAlertDialog(final long id){
		AlertDialog.Builder alert_confirm = new AlertDialog.Builder(mCtx);
		alert_confirm.setMessage(getString(R.string.framgent_memo_confirm_delete_memo)).setCancelable(false).setPositiveButton(getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteMemo(id);

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

	public void deleteMemo(long id){
		if(mMemoDataManager.deleteItemById(id)){
			Toast.makeText(mCtx, getString(R.string.message_removed), Toast.LENGTH_SHORT).show();
			mAlarmDataManager.resetMinAlarmCall();
		}else{
			Toast.makeText(mCtx, getString(R.string.message_failed), Toast.LENGTH_SHORT).show();
		}
		mMemoAdapter.notifyDataSetChanged();
	}

	private boolean insertRelation(AlarmVO alarmVO, MemoVO memoVO){
		RelationVO relationVO = new RelationVO();

		relationVO.setAlarmId(alarmVO.getId());
		relationVO.setfId(memoVO.getId());
		relationVO.setType(Const.ETC_TYPE.MEMO);
		return mCommonRelationDBManager.insert(relationVO);
	}
	private RecorderDataManager getRecorderDataManager(){
		if(mRecorderDataManager == null)
			mRecorderDataManager = new RecorderDataManager(mCtx);
		return mRecorderDataManager;
	}

	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		MemoDialogNew fragment = (MemoDialogNew) getFragmentManager().findFragmentByTag(Const.FRAGMENT_TAG.MEMO_DIALOG);
		if (fragment != null) {
			fragment.setTargetFragment(this, Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_CODE);
		}
	}

	@Override
	public void onDestroy() {
		//MainActivity.popActionbarInfo();
		EventBus.getDefault().post(new PopMessageEvent());
		super.onDestroy();
		Crashlytics.log(Log.DEBUG, this.toString(), "onDestroy");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Crashlytics.log(Log.DEBUG, this.toString(), "onDetach");
	}

	@Override
	public void onStop() {
		super.onStop();
		Crashlytics.log(Log.DEBUG, this.toString(), "onStop");
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
		void onFragmentInteraction(Uri uri);
	}

}
