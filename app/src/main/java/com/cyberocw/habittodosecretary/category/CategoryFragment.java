package com.cyberocw.habittodosecretary.category;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.MainActivity;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.alaram.AlarmFragment;
import com.cyberocw.habittodosecretary.alaram.vo.AlarmVO;
import com.cyberocw.habittodosecretary.category.vo.CategoryVO;
import com.cyberocw.habittodosecretary.memo.MemoFragment;
import com.cyberocw.habittodosecretary.memo.vo.MemoVO;

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
			isEtcMode = getArguments().getBoolean(Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_ETC_KEY, false);
			mParam2 = getArguments().getString(ARG_PARAM2);
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
		binBtnEvent();
	}

	private class CategoryClickListener implements AdapterView.OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			showMemoList(mCateDataManager.getItem(position).getId());
		}
	}

	private void binBtnEvent(){
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
	private void showCatePopup(long _id){
		if(_id == -1){

		}

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		//LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

		LinearLayout ll = new LinearLayout(mCtx);
		mEtCateTitle = new EditText(mCtx);
		mEtCateTitle.setLayoutParams(params);

		ll.setLayoutParams(params);
		int padding = (int) getResources().getDimension(R.dimen.catePopupPadding);
		ll.setPadding(padding, padding, padding, padding);

		ll.addView(mEtCateTitle);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(ll);
		builder.setTitle("Add Category");
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							addCateListener(dialog);

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

	public void addCateListener(DialogInterface dialog){
		String title = mEtCateTitle.getText().toString();

		if(title.equals("")){
			Toast.makeText(mCtx, "제목을 입력해 주세요", Toast.LENGTH_SHORT).show();
			return;
		}

		CategoryVO vo = new CategoryVO();
		vo.setTitle(title);
		vo.setType(Const.CATEGORY.TYPE);
		//vo.setSortOrder(mCateDataManager.getCount());

		if(mCateDataManager.addItem(vo)){
			Toast.makeText(mCtx, "추가 되었습니다", Toast.LENGTH_SHORT).show();
			dialog.dismiss();
			mCateAdapter.notifyDataSetChanged();
		}
		else{
			Toast.makeText(mCtx, "등록에 실패했습니다", Toast.LENGTH_SHORT).show();
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

		f.setTargetFragment(this, Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_ETC_CODE);

		f.setArguments(b);
		FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.addToBackStack(null)
				.replace(R.id.main_container, f).commit();
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

