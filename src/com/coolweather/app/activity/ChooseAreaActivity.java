package com.coolweather.app.activity;

import com.coolweather.app.R;
import com.coolweather.app.R.id;
import com.coolweather.app.R.layout;
import com.coolweather.app.R.menu;

import java.util.*;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;



import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

public class ChooseAreaActivity extends Activity{
	
	public static final int LEVEL_PROVINCE=0;
	public static final int LEVEL_CITY=1;
	public static final int LEVEL_COUNTY=2;
	
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList = new ArrayList<String>();
	
	/**
	 * ʡ�б�
	 */
	private List<Province> provinceList;
	/**
	 * ���б�
	 */
	private List<City> cityList;
	/**
	 * ���б�
	 */
	private List<County> countyList;
	/**
	 * ѡ�е�ʡ��
	 */
	private Province selectedProvince;
	/**
	 * ѡ�еĳ���
	 */
	private City selectedCity;
	/**
	 * ��ǰѡ�еļ���
	 */
	private int currentLevel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView=(ListView) findViewById(R.id.list_view);
		titleText =(TextView) findViewById(R.id.title_text);
		adapter =new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		coolWeatherDB=CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener(){
			
			@Override
			public void onItemClick(AdapterView<?> arg0,View view,int index,long arg3){
				if(currentLevel==LEVEL_PROVINCE){
					selectedProvince=provinceList.get(index);
					queryCities();

				} else if(currentLevel==LEVEL_CITY){
					selectedCity=cityList.get(index);
					queryCounties();
				}
			}
		});
		
		queryProvinces();//����ʡ������
	}
	
	/**
	 * ��ѯȫ�����е�ʡ�����ȴ����ݿ��в�ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ;
	 */
	private void queryProvinces(){
		provinceList=coolWeatherDB.loadProvinces();
		if(provinceList.size()>0){
			dataList.clear();
			for(Province p:provinceList){
				dataList.add(p.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);//��listView����Ϊ��ʼֵ;
			titleText.setText("�й�");
			currentLevel=LEVEL_PROVINCE;
			
		} else {
			queryFromServer(null,"province");
		}
	}
	/**
	 * ��ѯѡ�е�������;
	 */
	private void queryCities(){
		cityList=coolWeatherDB.loadCities(selectedProvince.getId());
		if(cityList.size()>0){
			dataList.clear();
			for(City c:cityList){
				dataList.add(c.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel=LEVEL_CITY;
			//Toast.makeText(ChooseAreaActivity.this, "queryCities��ִ����", Toast.LENGTH_SHORT).show();
		} else {
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}
	}
	
	/**
	 * 
	 * ��ѯѡ�����ڵ�������
	 */
	private void queryCounties(){
		countyList=coolWeatherDB.loadCounties(selectedCity.getId());
		if(countyList.size()>0){
			dataList.clear();
			for(County c : countyList){
				dataList.add(c.getCountyName());
				
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel=LEVEL_COUNTY;
			//Toast.makeText(ChooseAreaActivity.this, "queryCounties��ִ����", Toast.LENGTH_SHORT).show();
		} else {
			queryFromServer(selectedCity.getCityCode(),"county");
		}
	}
	
	/**
	 * ���ݴ���Ĵ��ź����ͣ��ӷ������ϲ�ѯ���ص����ݣ�
	 */
	
	private void queryFromServer(final String code, final String type){
		String address;
		if(!TextUtils.isEmpty(code)){
			address="http://www.weather.com.cn/data/list3/city"+code+".xml";
		} else {
			address="http://www.weather.com.cn/data/list3/city.xml";
		}
		
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			public void onFinish(String response){
				boolean result =false;
				if("province".equals(type)){
					result=Utility.handProvinceResponse(coolWeatherDB, response);
				} else if("city".equals(type)){
					result=Utility.handCityResponse(coolWeatherDB, response, selectedProvince.getId());
				} else if("county".equals(type)){
					result=Utility.handCountyResponse(coolWeatherDB, response, selectedCity.getId());
				}
				
				if(result){
					//ͨ��runOnUiThread�ص����̴߳����߼�;
					runOnUiThread(new Runnable() {
						@Override
						public void run(){
							closeProgressDialog();
							
							if("province".equals(type)){
								queryProvinces();
								
							} else if("city".equals(type)){
								queryCities();
								
							} else if("county".equals(type)){
								queryCounties();
								
							}
						}
					});
				}
			}
			

			
			@Override
			public void onError(Exception e){
				e.printStackTrace();
				//ͨ��runOnUiThread�ص����̴߳����߼���
				runOnUiThread(new Runnable() {
					@Override
					public void run(){
						closeProgressDialog();
						
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	/**
	 * ��ʾ���ȶԻ���
	 */
	private void showProgressDialog(){
		if(progressDialog==null){
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/**
	 * �رս��ȶԻ���
	 */
	private void closeProgressDialog(){
		if(progressDialog!=null){
			progressDialog.dismiss();
		}
	}
	
	/**
	 * ����Back�� ���ݵ�ǰ�ļ������ж� Ӧ�÷������б������б� ����Ӧ���˳�
	 */
	@Override
	public void onBackPressed() {
		if(currentLevel==LEVEL_COUNTY){
			queryCities();
		} else if(currentLevel==LEVEL_CITY){
			queryProvinces();
			
		} else {
			finish();
		}
	}

}