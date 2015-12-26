package com.cyberocw.habittodosecretary.memo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.cyberocw.habittodosecretary.Const;
import com.cyberocw.habittodosecretary.R;
import com.cyberocw.habittodosecretary.memo.ui.MemoDialogNew;
import com.cyberocw.habittodosecretary.memo.vo.MemoVO;
import com.getbase.floatingactionbutton.FloatingActionButton;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MemoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MemoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MemoFragment extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	MemoDataManager mMemoDataManager;
	MemoListAdapter mMemoAdapter;

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;

	private View mView;
	private Context mCtx;
	SharedPreferences mPrefs;
	private long mCateId = -1;

	private OnFragmentInteractionListener mListener;

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param param1 Parameter 1.
	 * @param param2 Parameter 2.
	 * @return A new instance of fragment MainFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static MemoFragment newInstance(String param1, String param2) {
		MemoFragment fragment = new MemoFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	public MemoFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
			mCateId = getArguments().getLong(Const.CATEGORY.CATEGORY_ID);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		mView = inflater.inflate(R.layout.fragment_memo, container, false);
		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		mCtx = getActivity();
		mPrefs = mCtx.getSharedPreferences(Const.ALARM_SERVICE_ID, mCtx.MODE_PRIVATE);

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
		Log.d(Const.DEBUG_TAG, "mCateId="+mCateId);
		mMemoDataManager = new MemoDataManager(mCtx, mCateId);
		mMemoAdapter = new MemoListAdapter(this, mCtx, mMemoDataManager);

		ListView lv = (ListView) mView.findViewById(R.id.memoListView);
		lv.setAdapter(mMemoAdapter);

		bindEvent();
	}

	private void bindEvent(){
		FloatingActionButton fab = (FloatingActionButton) mView.findViewById(R.id.fabAddAlarm);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(mCtx, "okclick", Toast.LENGTH_SHORT).show();
				showNewMemoDialog();
			}
		});
	}

	public void showNewMemoDialog(){
		showNewMemoDialog(-1);
	}

	public void showNewMemoDialog(long id) {

		MemoDialogNew dialogNew = new MemoDialogNew();
		Bundle bundle = new Bundle();

		if(id != -1) {
			bundle.putSerializable(Const.MEMO_VO, mMemoDataManager.getItemById(id));
			Log.d(Const.DEBUG_TAG, "selected memo=" + mMemoDataManager.getItemById(id).toString());

		}
		bundle.putSerializable(Const.CATEGORY.CATEGORY_ID, mCateId);

		dialogNew.setArguments(bundle);
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.replace(R.id.main_container, dialogNew);
		ft.addToBackStack(null).commit();
		dialogNew.setTargetFragment(this, Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		MemoVO vo;

		vo = (MemoVO) data.getExtras().getSerializable("memoVO");

		switch(resultCode) {

			case Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_FINISH_CODE :
				// 메모 추가
				if(mMemoDataManager.addItem(vo) == true)
					mMemoAdapter.notifyDataSetChanged();
				else
					Toast.makeText(mCtx, "DB에 삽입하는데 실패했습니다", Toast.LENGTH_LONG).show();
				break;
			case Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_MODIFY_FINISH_CODE :
				if(mMemoDataManager.modifyItem(vo) == true)
					mMemoAdapter.notifyDataSetChanged();
				else
					Toast.makeText(mCtx, "DB를 수정하는데 실패했습니다", Toast.LENGTH_LONG).show();
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}

	@Override
	public void onAttach(Activity activity) {
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
		public void onFragmentInteraction(Uri uri);
	}

}
