package com.cyberocw.habittodosecretary.memo.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.cyberocw.habittodosecretary.BuildConfig;
import com.cyberocw.habittodosecretary.WebViewActivity;
import com.cyberocw.habittodosecretary.common.vo.FileVO;
import com.cyberocw.habittodosecretary.file.AttachmentTask;
import com.cyberocw.habittodosecretary.file.FileDataManager;
import com.cyberocw.habittodosecretary.file.FileHelper;
import com.cyberocw.habittodosecretary.file.FileListAdapter;
import com.cyberocw.habittodosecretary.file.StorageHelper;
import com.cyberocw.habittodosecretary.memo.MemoFragment;
import com.cyberocw.habittodosecretary.util.IntentChecker;
import com.cyberocw.habittodosecretary.util.KeyboardUtils;
import com.cyberocw.habittodosecretary.file.OnAttachingFileListener;
import com.neopixl.pixlui.components.edittext.EditText;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.ui.AlarmDialogNew;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.category.CategoryDataManager;
import com.cyberocw.habittodosecretary.category.CategoryListAdapter;
import com.cyberocw.habittodosecretary.category.vo.CategoryVO;
import com.cyberocw.habittodosecretary.memo.vo.MemoVO;
import com.cyberocw.habittodosecretary.util.CommonUtils;


import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import it.feio.android.checklistview.Settings;
import it.feio.android.checklistview.exceptions.ViewNotSupportedException;
import it.feio.android.checklistview.interfaces.Constants;
import it.feio.android.checklistview.models.ChecklistManager;
import it.feio.android.pixlui.links.TextLinkClickListener;

/**
 * Created by cyberocw on 2015-12-14.
 */
public class MemoDialogNew extends Fragment implements com.cyberocw.habittodosecretary.file.OnAttachingFileListener{
	View mView;
	View switchView;
	LinearLayout mFileWrap;
	Context mCtx;
	EditText mTvTitle;
	Spinner mSpCategory;

	EditText mEtMemoEditor;
	TextView mTvMemoEditor;
	RatingBar mRatingBar;
	Button mBtnSave, mBtnEdit, mBtnTodo;
	Button mBtnAddAlarm;
	MemoVO mMemoVO;
	AlarmVO mAlarmVO;
	CategoryDataManager mCateDataManager;
	CategoryListAdapter mCateAdapter;
	ArrayList<CategoryVO> mArrayCategoryVOList = null;
	ImageView mBtnAttach;
	FileDataManager mFileDataManager;
	FileListAdapter mFileListAdapter;
	boolean mShareMode = false;
	SharedPreferences mPrefs;
	long mSelectedCateId = -1;
	boolean isMemoEditable = true;
	boolean isModifyAlarm = false;
	int mModifyMode = 0;
	long mInitAlarmId = -1;
	Uri mFileUri;
	LayoutInflater mInflater;
	String mAlarmFilePath = null;

	public PopupWindow mAttachmentDialog;

	boolean isChecklist;
	private ChecklistManager mChecklistManager;

	public MemoDialogNew() {
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {

		}
	}

	public void checkFileList(){
		File dir = mCtx.getExternalFilesDir(null);
		if(dir.isDirectory()){
			for(File file : dir.listFiles()){
				Log.d(this.toString(), "list name="+file.getName());
				//file.delete();
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		mView = inflater.inflate(R.layout.fragment_dialog_memo, container, false);
		mInflater = inflater;
		Toolbar toolbar = (Toolbar) mView.findViewById(R.id.toolbar);
		toolbar.setVisibility(View.VISIBLE);
		toolbar.setTitle(getResources().getString(R.string.dialog_title_memo));
		toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
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
		checkFileList();
		if(arguments != null) {
			mMemoVO = (MemoVO) arguments.getSerializable(Const.PARAM.MEMO_VO);
			mAlarmVO = (AlarmVO) arguments.getSerializable(Const.PARAM.ALARM_VO);

			if(mAlarmVO != null){
				try{
					mAlarmVO = (AlarmVO) mAlarmVO.clone();
				}catch(CloneNotSupportedException e){
					e.printStackTrace();
				}
				mInitAlarmId = mAlarmVO.getId();

			}
			mSelectedCateId = (long) arguments.getSerializable(Const.CATEGORY.CATEGORY_ID);

			if(arguments.containsKey(Const.MEMO.MEMO_INTERFACE_CODE.SHARE_MEMO_MODE))
				mShareMode = (boolean) arguments.getSerializable(Const.MEMO.MEMO_INTERFACE_CODE.SHARE_MEMO_MODE);

			if(mShareMode == true){

			}
			else if(mMemoVO != null) {
				try {
					mMemoVO = (MemoVO) mMemoVO.clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				mModifyMode = 1;
			}
			else
				mMemoVO = new MemoVO();

			isChecklist = arguments.getBoolean(Const.PARAM.IS_TODO, false);
		}
		else{
			Toast.makeText(mCtx, getString(R.string.dialog_memo_msg_category_id_not), Toast.LENGTH_SHORT).show();
			getFragmentManager().popBackStackImmediate();
		}

		mFileDataManager = new FileDataManager(mCtx);
		mFileListAdapter = new FileListAdapter(mCtx, mFileDataManager, 0);
		/*
		ListView lv = ButterKnife.findById(mView, R.id.lvFiles);
		lv.setAdapter(mFileListAdapter);
*/
		mPrefs = mCtx.getSharedPreferences(Const.ALARM_SERVICE_ID, mCtx.MODE_PRIVATE);

		initActivity();



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
		switchView = mEtMemoEditor = (EditText) mView.findViewById(R.id.etMemoEditor);
		mTvMemoEditor = (TextView) mView.findViewById(R.id.tvMemoEditor);
		mRatingBar = (RatingBar) mView.findViewById(R.id.ratingBar);
		mBtnSave = (Button) mView.findViewById(R.id.btnMemoSave);
		mBtnEdit = (Button) mView.findViewById(R.id.btnEdit);
		mBtnAddAlarm = (Button) mView.findViewById(R.id.btnAddAlarm);
		mBtnTodo = ButterKnife.findById(mView, R.id.btnTodo);
		mBtnAttach = ButterKnife.findById(mView, R.id.attachmentIcon);
		mFileWrap = ButterKnife.findById(mView, R.id.llFileWrap);
		if(mAlarmVO != null)
			mBtnAddAlarm.setText(getResources().getText(R.string.btn_memo_alarm_edit));

		makeCategoryList();

		bindEvent();
		init();
		//mInflater = getActivity().getLayoutInflater();

		CommonUtils.logCustomEvent("MemoDialogNew", "1");
		try {
			mFileDataManager.deleteTrash();
		}catch(Exception e){
			Crashlytics.log(Log.DEBUG, this.toString(), " trash error " + e.getMessage());
		}
	}

	private void init(){
		if(mModifyMode == 1 || mShareMode){
			mTvTitle.setText(mMemoVO.getTitle());
			mEtMemoEditor.setText(mMemoVO.getContents());
			//mTvMemoEditor.setText(mMemoVO.getContents());
			isMemoEditable = false;
			isChecklist = mMemoVO.getType().equals("TODO");
			mRatingBar.setRating((float) mMemoVO.getRank());
			mSelectedCateId = mMemoVO.getCategoryId();
			if(mMemoVO.getFileList() != null) {
				ArrayList<FileVO> fileList = (ArrayList<FileVO>) mMemoVO.getFileList().clone();
				Log.d(this.toString(), "fileList.size()=" + fileList.size());
				if (fileList != null && fileList.size() > 0) {
					mFileDataManager.setDataList(fileList);
					for (int i = 0; i < fileList.size(); i++) {
						this.attachFileView(fileList.get(i));
					}
				}

			}

		}

		if(mSelectedCateId != -1){
			for(int i = 0; i < mArrayCategoryVOList.size(); i++){
				if(mArrayCategoryVOList.get(i).getId() == mSelectedCateId){
					mSpCategory.setSelection(i);
					break;
				}
			}
		}
		if (isChecklist) {
			isChecklist = false;
			toggleCheckList();
		}
		initViewContent();
		//bindEventSaveAndEdit();
		CommonUtils.setupUI(mView, getActivity());

	}
	private void toggleCheckList() {
		View newView;

		/*
		 * Here is where the job is done. By simply calling an instance of the
		 * ChecklistManager we can call its methods.
		 */
		try {
			// Getting instance
			mChecklistManager = mChecklistManager == null ? new ChecklistManager(mCtx) : mChecklistManager;

			/*
			 * These method are useful when converting from EditText to
			 * ChecklistView (but can be set anytime, they'll be used at
			 * appropriate moment)
			 */

			// Setting new entries hint text (if not set no hint
			// will be used)
			mChecklistManager.newEntryHint(mPrefs.getString("settings_hint", "Input Text"));
			// Let checked items are moved on bottom

			mChecklistManager.moveCheckedOnBottom(Integer.valueOf(mPrefs.getString("settings_checked_items_behavior",
					String.valueOf(Settings.CHECKED_ON_BOTTOM))));

			// Is also possible to set a general changes listener
			//mChecklistManager.setCheckListChangedListener(this);


			/*
			 * These method are useful when converting from ChecklistView to
			 * EditText (but can be set anytime, they'll be used at appropriate
			 * moment)
			 */

			// Decide if keep or remove checked items when converting
			// back to simple text from checklist
			mChecklistManager.linesSeparator(mPrefs.getString("settings_lines_separator", Constants.LINES_SEPARATOR));

			// Decide if keep or remove checked items when converting
			// back to simple text from checklist
			mChecklistManager.keepChecked(mPrefs.getBoolean("settings_keep_checked", Constants.KEEP_CHECKED));

			// I want to make checks symbols visible when converting
			// back to simple text from checklist
			mChecklistManager.showCheckMarks(mPrefs.getBoolean("settings_show_checks", true));

			// Enable or disable drag & drop
			mChecklistManager.dragEnabled(false);
			mChecklistManager.dragVibrationEnabled(false);

			// Converting actual EditText into a View that can
			// replace the source or viceversa
			newView = mChecklistManager.convert(switchView);

			// Replacing view in the layout
			mChecklistManager.replaceViews(switchView, newView);

			// Updating the instance of the pointed view for
			// eventual reverse conversion
			switchView = newView;

			isChecklist = !isChecklist;

			KeyboardUtils.hideKeyboard(mView);
		} catch (ViewNotSupportedException e) {
			// This exception is fired if the source view class is not supported
			e.printStackTrace();
		}
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
			mBtnEdit.setText(getString(R.string.btn_memo_finish));

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

		if(mModifyMode == 1) {
			Button delete = ButterKnife.findById(mView, R.id.btnMemoDelete);
			delete.setVisibility(View.VISIBLE);
			delete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					deleteItemAlertDialog();
				}
			});
		}

		mBtnTodo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleCheckList();
			}
		});
		mBtnAttach.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showPopup(v);

			}
		});
	}

	private void initViewContent() {

		//mEtMemoEditor.setText(noteTmp.getContent());
		mEtMemoEditor.gatherLinksForText();
		mEtMemoEditor.setOnTextLinkClickListener(textLinkClickListener);
		// Avoids focused line goes under the keyboard
		//mEtMemoEditor.addTextChangedListener(this);

	}

	public void deleteItemAlertDialog(){
		AlertDialog.Builder alert_confirm = new AlertDialog.Builder(mCtx);
		alert_confirm.setMessage(getString(R.string.fragment_memo_msg_delete_confirm)).setCancelable(false).setPositiveButton(getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						returnDataDelete();
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

	private void btnAddAlarmPopup(){
		if(mAlarmVO == null || mAlarmVO.getId() == -2) {
			showAlarmPopup();
		}else{
			String names[] ={getString(R.string.edit),getString(R.string.delete)};
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(mCtx);

			ListView lv = new ListView(mCtx);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			alertDialog.setView(lv);
			alertDialog.setTitle(getString(R.string.option));

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
							mBtnAddAlarm.setText(getResources().getText(R.string.btn_memo_alarm));
							break;
					}
					dialogInterface.dismiss();
				}
			});
		}
	}

	private void deleteAlarm(){
		if(mModifyMode == 1 && mAlarmVO.getId() > -1) {
			//mAlarmVO.setId(-2);
			mAlarmVO = null;
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

		transaction.add(R.id.warpContainer, alarmDialogNew, Const.FRAGMENT_TAG.ALARM_DIALOG)
				.addToBackStack(null).commit();


	}

	private void showPopup(View anchor) {
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

		// Inflate the popup_layout.xml
		LayoutInflater inflater = (LayoutInflater) mCtx.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.attachment_dialog, null);

		// Creating the PopupWindow
		mAttachmentDialog = new PopupWindow(mCtx);
		mAttachmentDialog.setContentView(layout);
		mAttachmentDialog.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		mAttachmentDialog.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		mAttachmentDialog.setFocusable(true);
		/*mAttachmentDialog.setOnDismissListener(() -> {
			if (isRecording) {
				isRecording = false;
				stopRecording();
			}
		});*/

		// Clear the default translucent background
		mAttachmentDialog.setBackgroundDrawable(new BitmapDrawable());

		// Camera
		android.widget.TextView cameraSelection = (android.widget.TextView) layout.findViewById(R.id.camera);
		cameraSelection.setOnClickListener(new AttachmentOnClickListener());
		// Audio recording
		/*android.widget.TextView recordingSelection = (android.widget.TextView) layout.findViewById(R.id.recording);
		recordingSelection.setOnClickListener(new AttachmentOnClickListener());*/
		// Video recording
		/*android.widget.TextView videoSelection = (android.widget.TextView) layout.findViewById(R.id.video);
		videoSelection.setOnClickListener(new AttachmentOnClickListener());*/
		// Files
		android.widget.TextView filesSelection = (android.widget.TextView) layout.findViewById(R.id.files);
		filesSelection.setOnClickListener(new AttachmentOnClickListener());
		/*// Sketch
		android.widget.TextView sketchSelection = (android.widget.TextView) layout.findViewById(R.id.sketch);
		sketchSelection.setOnClickListener(new AttachmentOnClickListener());
		// Location
		android.widget.TextView locationSelection = (android.widget.TextView) layout.findViewById(R.id.location);
		locationSelection.setOnClickListener(new AttachmentOnClickListener());
		// Desktop note with PushBullet
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			android.widget.TextView pushbulletSelection = (android.widget.TextView) layout.findViewById(R.id
					.pushbullet);
			pushbulletSelection.setVisibility(View.VISIBLE);
			pushbulletSelection.setOnClickListener(new AttachmentOnClickListener());
		}*/

		try {
			mAttachmentDialog.showAsDropDown(anchor);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void takePhoto() {
		// Checks for camera app available
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (!IntentChecker.isAvailable(mCtx, intent, new String[]{PackageManager.FEATURE_CAMERA})) {
			Toast.makeText(mCtx, "feature_not_available_on_this_device", Toast.LENGTH_SHORT).show();

			return;
		}
		// Checks for created file validity
		File f = StorageHelper.createNewAttachmentFile(getActivity(), ".jpeg");
		Uri uri = FileProvider.getUriForFile(mCtx, BuildConfig.APPLICATION_ID + ".provider", f);
		if (f == null) {
			Toast.makeText(mCtx, getString(R.string.error), Toast.LENGTH_SHORT).show();
			return;
		}
		// Launches intent
		mFileUri = Uri.fromFile(f);

		List<ResolveInfo> resInfoList = mCtx.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		for (ResolveInfo resolveInfo : resInfoList) {
			String packageName = resolveInfo.activityInfo.packageName;
			mCtx.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
		}

		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		startActivityForResult(intent, Const.MEMO.MEMO_INTERFACE_CODE.TAKE_PHOTO);
	}
	private class AttachmentOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				// Photo from camera
				case R.id.camera:
					takePhoto();
					//mAttachmentDialog.dismiss();
					mAttachmentDialog.dismiss();
					break;

				case R.id.files:
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					intent.setType("*/*");
					startActivityForResult(intent,Const.MEMO.MEMO_INTERFACE_CODE.PICK_FILE_RESULT_CODE);
					mAttachmentDialog.dismiss();
					break;
				default:
					Log.e(Constants.TAG, "Wrong element choosen: " + v.getId());
					mAttachmentDialog.dismiss();
			}
		}
	}

	public void takeVideo() {

	}

	private void dataBind(){
		String title =  mTvTitle.getText().toString();
		mMemoVO.setCategoryId(mSelectedCateId);
		mMemoVO.setTitle(title);
		String text = "";
		if(isChecklist) {
			try {
				text = ((EditText) mChecklistManager.convert(switchView)).getText().toString();
			} catch (ViewNotSupportedException e) {
				e.printStackTrace();
			}
		}
		else{
			text = mEtMemoEditor.getText().toString();
		}
		mMemoVO.setContents(text);
		mMemoVO.setRank((int) mRatingBar.getRating());
		mMemoVO.setType(isChecklist ? "TODO" : "MEMO");
	}

	private boolean validate(){
		if(mSelectedCateId == -1){
			Toast.makeText(mCtx, "카테고리를 선택해 주세요", Toast.LENGTH_SHORT).show();
			return false;
		}

		if(mTvTitle.getText().toString().equals("")){
			Toast.makeText(mCtx, getString(R.string.dialog_memo_msg_no_title), Toast.LENGTH_SHORT).show();
			return false;
		}

		return true;
	}

	private void returnData(){
		KeyboardUtils.hideKeyboard(mView);
		dataBind();
		if(validate() == false){
			return ;
		}

        ArrayList<FileVO> fileVOList =  mFileDataManager.getDataList();
        if(fileVOList != null && fileVOList.size() > 0) {
            //bundle.putSerializable(Const.PARAM.FILE_VO_LIST, fileVOList);
            mMemoVO.setFileList(fileVOList);
        }

		ArrayList<FileVO> delFileVOList = mFileDataManager.getDelDataList();
		if(delFileVOList != null && delFileVOList.size() > 0) {
			//bundle.putSerializable(Const.PARAM.FILE_VO_LIST, fileVOList);
			mMemoVO.setDelFileList(delFileVOList);
		}

		Bundle bundle = new Bundle();
		bundle.putSerializable(Const.PARAM.MEMO_VO, mMemoVO);

		if(mInitAlarmId > -1 && isModifyAlarm){
			bundle.putLong(Const.MEMO.ORIGINAL_ALARM_ID_KEY, mInitAlarmId);
		}

		if(isModifyAlarm && mAlarmVO != null) {
			bundle.putSerializable(Const.PARAM.ALARM_VO, mAlarmVO);
		}
		//알람의 type만 변경한 경우
		if(isModifyAlarm && mAlarmFilePath != null)
			bundle.putString(Const.PARAM.FILE_PATH, mAlarmFilePath);

		Intent intent = new Intent();
		intent.putExtras(bundle);

		int returnCode = mModifyMode == 1 ? Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_MODIFY_FINISH_CODE : Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_FINISH_CODE;

		Log.d(this.toString(), "getTargetFragment()="+getTargetFragment() + " getTargetRequestCode()="+getTargetRequestCode());

		getTargetFragment().onActivityResult(getTargetRequestCode(), returnCode, intent);
		getActivity().getSupportFragmentManager().popBackStackImmediate();
	}

	private void returnDataDelete(){
		Bundle bundle = new Bundle();
		bundle.putSerializable(Const.PARAM.MEMO_VO, mMemoVO);
		Intent intent = new Intent();
		intent.putExtras(bundle);
		getTargetFragment().onActivityResult(getTargetRequestCode(), Const.MEMO.MEMO_INTERFACE_CODE.DEL_MEMO_FINISH_CODE, intent);
		getActivity().getSupportFragmentManager().popBackStackImmediate();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		FileVO vo;
		switch (requestCode){
			case Const.ALARM_INTERFACE_CODE.ADD_ALARM_CODE :
				isModifyAlarm = true;
				mAlarmVO = (AlarmVO) intent.getExtras().getSerializable(Const.PARAM.ALARM_VO);
				String fromPath = intent.getExtras().getString(Const.PARAM.FILE_PATH, null);
				mAlarmFilePath = fromPath;

				if (mAlarmVO.getAlarmOption() == Const.ALARM_OPTION_TO_SOUND.RECORD) {
					ArrayList<FileVO> arrFile = new ArrayList<>();
					FileVO fVO = new FileVO();
					fVO.setUriPath(fromPath);
					arrFile.add(fVO);
					mAlarmVO.setFileList(arrFile);
					if (fromPath == null) {
						Toast.makeText(mCtx, mCtx.getString(R.string.permission_record_cont), Toast.LENGTH_SHORT).show();
						return;
					}
				}
				mBtnAddAlarm.setText(getResources().getText(R.string.btn_memo_alarm_edit));
				break;
			case Const.MEMO.MEMO_INTERFACE_CODE.PICK_FILE_RESULT_CODE :
				if(resultCode == Activity.RESULT_OK){
					onActivityResultManageReceivedFiles(intent);

					//textFile.setText(FilePath);
				}
				break;
			case Const.MEMO.MEMO_INTERFACE_CODE.TAKE_PHOTO :
				vo = new FileVO(mFileUri, Const.MIME_TYPE_IMAGE);
				//onActivityResultManageReceivedFiles(intent);
				onAttachingFileFinished(vo);
				//addAttachment(attachment);
				//mAttachmentAdapter.notifyDataSetChanged();

		}

		super.onActivityResult(requestCode, resultCode, intent);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void onActivityResultManageReceivedFiles(Intent intent) {
		List<Uri> uris = new ArrayList<>();
		if (Build.VERSION.SDK_INT > 16 && intent.getClipData() != null) {
			for (int i = 0; i < intent.getClipData().getItemCount(); i++) {
				uris.add(intent.getClipData().getItemAt(i).getUri());
			}
		} else {
			uris.add(intent.getData());
		}
		for (Uri uri : uris) {
			String fileName = FileHelper.getNameFromUri(getActivity(), uri);
			new AttachmentTask(mCtx, this, uri, fileName, this).execute();
		}
	}

	TextLinkClickListener textLinkClickListener = new TextLinkClickListener() {
		@Override
		public void onTextLinkClick(View view, final String clickedString, final String url) {
			AlertDialog.Builder alert_confirm = new AlertDialog.Builder(mCtx);
			alert_confirm.setMessage(clickedString).setCancelable(false).setPositiveButton("open",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							try {
								openWebView(url);
							} catch (NullPointerException e) {
							}
							dialog.dismiss();
						}
					}).setNegativeButton("copy",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// 'No'
							android.content.ClipboardManager clipboard = (android.content.ClipboardManager) mCtx.getSystemService(Activity.CLIPBOARD_SERVICE);
							android.content.ClipData clip = android.content.ClipData.newPlainText("text label", clickedString);
							clipboard.setPrimaryClip(clip);
							dialog.dismiss();
						}
					});
			AlertDialog alert = alert_confirm.create();
			alert.show();

			View clickedView = isChecklist ? switchView : mEtMemoEditor;
			clickedView.clearFocus();
			KeyboardUtils.hideKeyboard(clickedView);
			new Handler().post(new Runnable() {
				@Override
				public void run() {
					View clickedView = isChecklist ? switchView : mEtMemoEditor;
					KeyboardUtils.hideKeyboard(clickedView);
				}
			});

		}
	};
	public void openWebView(String url) {
		//WebViewDialog dialog = new WebViewDialog();
		Intent intent = new Intent(mCtx, WebViewActivity.class);

		Bundle bundle = new Bundle();

		bundle.putSerializable("url", url);
		intent.putExtras(bundle);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	@Override
	public void onAttachingFileErrorOccurred(FileVO mAttachment) {

	}

	@Override
	public void onAttachingFileFinished(FileVO vo) {
		vo.setType(Const.ETC_TYPE.MEMO);
		if(vo.getName() == null || vo.getName().equals("null")){
			String fileName = FileHelper.getNameFromUri(getActivity(), Uri.parse(vo.getUri()));
			vo.setName(fileName);
		}

		mFileDataManager.addItem(vo);

		attachFileView(vo);

		//llFileWrap
		//mFileListAdapter.notifyDataSetChanged();
	}

	public void attachFileView(final FileVO vo){
		Crashlytics.log(Log.DEBUG, this.toString(), "mAttachment="+vo.toString());
		View iv = mInflater.inflate(R.layout.view_file, null);
		TextView tv = ButterKnife.findById(iv, R.id.tvTitle);
		tv.setText(vo.getName());
		iv.setLongClickable(true);
        iv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longClickPopup(vo.getId(), v);
                return true;
            }
        });
		iv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FileVO attachment = vo;
				//Uri uri = Uri.parse(attachment.getUri());
				Intent attachmentIntent;

				Uri uri = FileProvider.getUriForFile(mCtx, BuildConfig.APPLICATION_ID + ".provider", new File(attachment.getUriPath()));

				if (Const.MIME_TYPE_FILES.equals(attachment.getMimeType()) || Const.MIME_TYPE_AUDIO.equals(attachment.getMimeType())) {

					attachmentIntent = new Intent(Intent.ACTION_VIEW);
					attachmentIntent.setDataAndType(uri, StorageHelper.getMimeType(mCtx,
							Uri.parse(attachment.getUri())));
					attachmentIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent
							.FLAG_GRANT_WRITE_URI_PERMISSION);
					if (IntentChecker.isAvailable(mCtx, attachmentIntent, null)) {
						startActivity(attachmentIntent);
					} else {
						//mCtx.showMessage(R.string.feature_not_available_on_this_device, ONStyle.WARN);
						Toast.makeText(mCtx, "feature_not_available_on_this_device", Toast.LENGTH_SHORT).show();
						v.performLongClick();
					}
					// Media files will be opened in internal gallery
				} else if (Const.MIME_TYPE_IMAGE.equals(attachment.getMimeType())
						|| Const.MIME_TYPE_SKETCH.equals(attachment.getMimeType())
						|| Const.MIME_TYPE_VIDEO.equals(attachment.getMimeType())) {
					int clickedImage = 0;
					attachmentIntent = new Intent(Intent.ACTION_VIEW);
					attachmentIntent.setDataAndType(uri, StorageHelper.getMimeType(mCtx,
							Uri.parse(attachment.getUri())));
					attachmentIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent
							.FLAG_GRANT_WRITE_URI_PERMISSION);

					startActivity(attachmentIntent);

				/*
				ArrayList<FileVO> images = new ArrayList<>();
				for (FileVO mAttachment : mMemoVO.getFileList()) {
					if (Const.MIME_TYPE_IMAGE.equals(mAttachment.getMimeType())
							|| Const.MIME_TYPE_SKETCH.equals(mAttachment.getMimeType())
							|| Const.MIME_TYPE_VIDEO.equals(mAttachment.getMimeType())) {
						images.add(mAttachment);
						if (mAttachment.equals(attachment)) {
							clickedImage = images.size() - 1;
						}
					}
				}*/



					// Intent
				/*attachmentIntent = new Intent(mCtx, GalleryActivity.class);
				attachmentIntent.putExtra(Constants.GALLERY_TITLE, title1);
				attachmentIntent.putParcelableArrayListExtra(Constants.GALLERY_IMAGES, images);
				attachmentIntent.putExtra(Constants.GALLERY_CLICKED_IMAGE, clickedImage);
				startActivity(attachmentIntent);*/

				} else if (Const.MIME_TYPE_AUDIO.equals(attachment.getMimeType())) {
					//playback(v, attachment.getUri());
				}

			}
		});

		//ButterKnife.findById(convertView, R.id.ivImage).setVisibility(View.GONE);
		Uri uri = Uri.parse(vo.getUri());

		if(vo.getMimeType().equals(Const.MIME_TYPE_IMAGE)){
			ImageView imageView = ButterKnife.findById(iv, R.id.ivImage);
			imageView.setVisibility(View.VISIBLE);
			Uri thumbnailUri = uri;

			Glide.with(mCtx)
					.load(thumbnailUri)
					//.centerCrop()
					.thumbnail(0.4f)
					.crossFade()
					.into(imageView);
		}
		mFileWrap.addView(iv);
	}

    private void longClickPopup(final long _id, final View iv){
        String names[] ={mCtx.getString(R.string.share), mCtx.getString(R.string.delete)};
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mCtx);

        ListView lv = new ListView(mCtx);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        alertDialog.setView(lv);
        alertDialog.setTitle(mCtx.getString(R.string.option));

        lv.setLayoutParams(params);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mCtx,android.R.layout.simple_list_item_1,names);
        lv.setAdapter(adapter);

        final DialogInterface dialogInterface = alertDialog.show();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
						Intent shareIntent = new Intent(Intent.ACTION_SEND);
						FileVO attachment = mFileDataManager.getById(_id);
						File shareFile = new File(attachment.getUriPath());

						Uri newUri = FileProvider.getUriForFile(mCtx, BuildConfig.APPLICATION_ID + ".provider", shareFile);
						if (shareFile == null) {
							Toast.makeText(mCtx, getString(R.string.error), Toast.LENGTH_SHORT).show();
							return;
						}
						shareIntent.setType(StorageHelper.getMimeType(mCtx, newUri));
						shareIntent.putExtra(Intent.EXTRA_STREAM, newUri);
						shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

						if (IntentChecker.isAvailable(mCtx, shareIntent, null)) {
							startActivity(shareIntent);
						} else {
							Toast.makeText(mCtx, "Failed couldn't share on this device" , Toast.LENGTH_SHORT).show();
						}

                        break;
                    case 1:
                        if(mModifyMode == 0){
                            mFileDataManager.delete(_id);
                        }
                        else{
							mFileDataManager.deleteInList(_id);
							mFileDataManager.addDeleteItem(_id);
						}
						((ViewGroup) iv.getParent()).removeView((ViewGroup)iv);
                        break;
                }
                dialogInterface.dismiss();
            }
        });
    }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(this.toString(), "onSaveInstanceState");
		//setTargetFragment(null, -1);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		MemoFragment fragment = (MemoFragment) getFragmentManager().findFragmentByTag(Const.FRAGMENT_TAG.MEMO);
		Log.d(this.toString(), "on attach fragment="+fragment);
		if (fragment != null) {
			setTargetFragment(fragment, Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_CODE);
			Log.d(this.toString(), "settargetFragment ok");
		}
	}
}
