package com.cyberocw.habittodosecretary.category;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.category.vo.CategoryVO;
import com.cyberocw.habittodosecretary.memo.MemoFragment;
import com.cyberocw.habittodosecretary.util.CommonUtils;

/**
 * Created by cyberocw on 2015-12-06.
 */
public class CategoryFragment extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;
	private ActionBar mActionBar = null;
	private View mView;
	private Context mCtx;
	SharedPreferences mPrefs;
	CategoryDataManager mCateDataManager;
	CategoryListAdapter mCateAdapter;
	private boolean isEtcMode;
	private OnFragmentInteractionListener mListener;
	private AlertDialog mCatePopupBilder;
	private EditText mEtCateTitle;

	public CategoryFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
		/*	isEtcMode = getArguments().getBoolean(Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_ETC_KEY, false);
			mParam2 = getArguments().getString(ARG_PARAM2);*/
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		mView = inflater.inflate(R.layout.fragment_cate, container, false);
		return mView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		/*outState.putInt(Const.PARAM.MODE, mMode);
		outState.putLong(Const.PARAM.ALARM_ID, mAlarmId);
		*/
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		mCtx = getActivity();
		mPrefs = mCtx.getSharedPreferences(Const.ALARM_SERVICE_ID, Context.MODE_PRIVATE);
		initActivity();
	}

	private void initActivity(){
		mCateDataManager = new CategoryDataManager(mCtx);
		mCateAdapter = new CategoryListAdapter(this, mCtx, mCateDataManager);

		ListView lv = (ListView) mView.findViewById(R.id.categoryListView);
		lv.setAdapter(mCateAdapter);
		lv.setOnItemClickListener(new CategoryClickListener());
		lv.setLongClickable(true);

		lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				longClickPopup(position, id);
				return true;
			}
		});
		bindBtnEvent();
		CommonUtils.logCustomEvent("CategoryFragment", "1", "category count", mCateDataManager.getCount());

	}

	private class CategoryClickListener implements AdapterView.OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			showMemoList(mCateDataManager.getItem(position).getId());
		}
	}

	private void bindBtnEvent(){
		Button btnAddCate = (Button) mView.findViewById(R.id.btnAddCate);
		btnAddCate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showCatePopup();
			}
		});
	}

	private void showCatePopup(){
		showCatePopup(-1);
	}
	private void showCatePopup(long cateId){
		final CategoryVO categoryVO;
		String dialogTitle = "";
		if(cateId > -1){
			categoryVO = mCateDataManager.getItemById(cateId);
		}else{
			categoryVO = new CategoryVO();
		}
		dialogTitle = getString(R.string.dialog_category_title);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		//LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

		LinearLayout ll = new LinearLayout(mCtx);
		mEtCateTitle = new EditText(mCtx);
		mEtCateTitle.setLayoutParams(params);
		mEtCateTitle.setText(categoryVO.getTitle());

		ll.setLayoutParams(params);
		int padding = (int) getResources().getDimension(R.dimen.catePopupPadding);
		ll.setPadding(padding, padding, padding, padding);

		ll.addView(mEtCateTitle);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(ll);
		builder.setTitle(dialogTitle);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							addCateListener(dialog, categoryVO);

						}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User cancelled the dialog
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	public void addCateListener(DialogInterface dialog, CategoryVO categoryVO){

		String title = mEtCateTitle.getText().toString();

		if(title.equals("")){
			Toast.makeText(mCtx, getString(R.string.dialog_cate_msg_subject), Toast.LENGTH_SHORT).show();
			return;
		}

		categoryVO.setTitle(title);
		categoryVO.setType(Const.CATEGORY.TYPE);
		//vo.setSortOrder(mCateDataManager.getCount());
		Crashlytics.log(Log.DEBUG, this.toString(), "categoryVO.getId()="+categoryVO.getId());
		if(categoryVO.getId() == -1){
			if(mCateDataManager.addItem(categoryVO))
				Toast.makeText(mCtx, getString(R.string.success), Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(mCtx, getString(R.string.db_failed_generate_id), Toast.LENGTH_LONG).show();

		}else{
			if(mCateDataManager.modifyItem(categoryVO))
				Toast.makeText(mCtx, getString(R.string.success), Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(mCtx, getString(R.string.msg_failed_modify), Toast.LENGTH_LONG).show();
		}
		dialog.dismiss();
		mCateAdapter.notifyDataSetChanged();

	}
	protected void longClickPopup(int position, final long _id){
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
						showCatePopup(_id);
						//showNewTimerDialog(_id);

						break;
					case 1:
						deleteItemAlertDialog(_id);
				}
				dialogInterface.dismiss();
			}
		});


	}
	public void deleteItemAlertDialog(final long id){
		AlertDialog.Builder alert_confirm = new AlertDialog.Builder(mCtx);
		alert_confirm.setMessage(getString(R.string.category_message_del)).setCancelable(false).setPositiveButton(getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						deleteCategory(id);

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

	private void deleteCategory(long id){
		if(mCateDataManager.deleteItemById(id)){
			Toast.makeText(mCtx, getString(R.string.message_removed), Toast.LENGTH_LONG).show();
			mCateAdapter.notifyDataSetChanged();
		}else{
			Toast.makeText(mCtx, getString(R.string.message_failed), Toast.LENGTH_LONG).show();
		}
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}

	@Override
	public void onAttach(Context activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	public void showMemoList(long id) {
		Fragment f = new MemoFragment();
		//Bundle b = new Bundle();
		Bundle b = getArguments();
		if(b == null)
			b = new Bundle();


		b.putLong(Const.CATEGORY.CATEGORY_ID, id);
		b.putString(Const.CATEGORY.CATEGORY_TITLE_KEY, mCateDataManager.getItemById(id).getTitle());
		b.putBoolean(Const.MEMO.SHOW_TOOLBAR, true);
		f.setTargetFragment(this, Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_ETC_CODE);

		mActionBar.setTitle(mCateDataManager.getItemById(id).getTitle());

		f.setArguments(b);
		FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.addToBackStack(null)
				.add(R.id.main_container, f).commit();

	}

	public void setActionBar(ActionBar actionBar){
		mActionBar = actionBar;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(resultCode) {
			case Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_ETC_CODE:
				getTargetFragment().onActivityResult(requestCode, resultCode, data);
				getActivity().getSupportFragmentManager().popBackStackImmediate();
				break;
		}
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
		// TODO: Update argument type and name
		void onFragmentInteraction(Uri uri);
	}
}

