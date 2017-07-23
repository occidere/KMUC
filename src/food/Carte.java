package food;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.LinkedList;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Carte {
	private transient final String ADDRESS = "http://kmucoop.kookmin.ac.kr/menu/menujson.php";
	private transient String sdate, edate, today, reqAddress;
	private transient JSONObject allMenu;
	private transient List<String> menuByTime[];//아침 점심 저녁
	private transient boolean isSuccess;
	
	/* 날짜 지정 없으면 오늘 요일로 자동 호출 */
	public Carte(){
		this(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
	}
	
	/* 별도의 날짜 지정 가능 yyyy-MM-dd 형태 */
	public Carte(String date) {
		init(date);
	}
	
	private void init(String date){
		sdate = edate = today = date;
		reqAddress = String.format("%s?sdate=%s&edate=%s&today=%s", ADDRESS, sdate, edate, today);
		
		try{
			isSuccess = true;
			allMenu = getAllMenuJson(reqAddress);
		}
		catch(Exception e){
			System.err.println("JSON 파싱 실패");
			e.printStackTrace();
			isSuccess = false;
		}
	}
	
	/**
	 * 네트워크 연결을 통한 모든 메뉴 JSON 파싱
	 * @param reqAddress 모든 파라미터가 포함된 리퀘스트 주소
	 * @return 모든 메뉴가 담긴 JSONObject
	 * @throws IOException
	 * @throws ParseException
	 */
	private JSONObject getAllMenuJson(String reqAddress) throws IOException, ParseException {
		HttpURLConnection conn = (HttpURLConnection)new URL(reqAddress).openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("User-Agent", "Mozilla/5.0");
		conn.setConnectTimeout(5000);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		StringBuilder sb = new StringBuilder();
		
		while((line = br.readLine()) != null){
			sb.append(line);
		}
		br.close();
		
		
		JSONParser parser = new JSONParser();
		JSONObject allMenu = (JSONObject)parser.parse(replace(sb.toString()));

		return allMenu;
	}
	
	/**
	 * 법식 파싱
	 * @param allMenu
	 * @return
	 */
	private String[] parseBubsik(JSONObject allMenu){
		String[] bubsikMenu = {
				"石火랑 조식",
				"바로바로1", "바로바로2", "밥이랑 하나", "밥이랑 두울", "면이랑", "石火랑"
		};
		
		JSONObject bubsik = (JSONObject)((JSONObject)allMenu.get("한울식당")).get(today);
		
		/* 법식의 경우도 일부 식당에선 "메뉴" 부분에 메뉴랑 가격 다 몰아놓음 */
		
		int i, n = bubsikMenu.length;
		String menuInfo[], bubsikMenuArr[] = new String[n];
		for(i=0;i<n;i++){
			menuInfo = parseMenuInfo((JSONObject)bubsik.get(bubsikMenu[i]));
			menuInfo[0] = menuInfo[0].replace("\\", "￦");
			//bubsikMenuArr[i] = String.format("[%s] %s", bubsikMenu[i], getOneline(menuInfo));
			bubsikMenuArr[i] = String.format("- %s", getOneline(menuInfo));
		}
		return bubsikMenuArr;
	}

	
	/**
	 * 학식 파싱
	 * @param allMenu 전체 메뉴 JSONObject
	 */
	private String[] parseHaksik(JSONObject allMenu){
		String[] haksikMenu = {
				"착한아침",
				"가마 중식", "인터쉐프 중식", "데일리밥 중식", "누들송 (카페테리아) 중식", "누들송(면) 중식", 
				"인터쉐프 석식", "데일리밥 석식", "가마 석식"
		};
		JSONObject haksik = (JSONObject)((JSONObject)allMenu.get("학생식당")).get(today);
		
		int i, n = haksikMenu.length;
		String menuInfo[], haksikMenuArr[] = new String[n];
		for(i=0;i<n;i++){
			menuInfo = parseMenuInfo((JSONObject)haksik.get(haksikMenu[i]));
			//haksikMenuArr[i] = String.format("[%s] %s", haksikMenu[i], getOneline(menuInfo));
			haksikMenuArr[i] = String.format("- %s", getOneline(menuInfo));
		}
		return haksikMenuArr;
	}
	
	/**
	 * 차이웨이 파싱.</br>
	 * @param allMenu
	 */
	private String[] parseChiway(JSONObject allMenu){
		String[] chiwayMenu = { "차이웨이 상시", "차이웨이 특화" };

		JSONObject haksik = (JSONObject)((JSONObject)allMenu.get("학생식당")).get(today);
		
		/* 차이웨이의 경우 "메뉴":"가격" 중 메뉴에 모든 정보가 들어있고, 가격은 공백임 */
		int i, n = chiwayMenu.length;
		String menuInfo[], chiwayMenuArr[] = new String[n];
		for(i=0;i<n;i++){
			menuInfo = parseMenuInfo((JSONObject)haksik.get(chiwayMenu[i]));
			menuInfo[0] = menuInfo[0].replace("\\", "￦");
			//chiwayMenuArr[i] = String.format("[%s] %s", chiwayMenu[i], getOneline(menuInfo));
			chiwayMenuArr[i] = String.format("- %s", getOneline(menuInfo));
		}
		return chiwayMenuArr;
	}
	
	/**
	 * 교직원식당 파싱
	 * @param allMenu
	 * @return
	 */
	private String[] parseFaculty(JSONObject allMenu){
		String facultyMenu[] = { "키친1", "키친2", "주문식", "석식" };
		
		JSONObject faculty = (JSONObject)((JSONObject)allMenu.get("교직원식당")).get(today);
		
		int i, n = facultyMenu.length;
		String[] menuInfo, facultyMenuArr = new String[n];
		for(i=0;i<n;i++){
			menuInfo = parseMenuInfo((JSONObject)faculty.get(facultyMenu[i]));
			//facultyMenuArr[i] = String.format("[%s] %s", facultyMenu[i], getOneline(menuInfo));
			facultyMenuArr[i] = String.format("- %s", getOneline(menuInfo));
		}
		
		return facultyMenuArr;
	}
	
	/**
	 * 청향 파싱
	 * @param allMenu
	 * @return
	 */
	private String[] parseChunghyang(JSONObject allMenu){
		String chunghyangMenu[] = {
				"메뉴1", "메뉴2", "메뉴3", "메뉴4", "메뉴5", "메뉴6", "메뉴7"
		};
		
		JSONObject chunghyang = (JSONObject)((JSONObject)allMenu.get("청향")).get(today);
		
		int i, n = chunghyangMenu.length;
		String[] menuInfo, chunghyangMenuArr = new String[n];
		for(i=0;i<n;i++){
			menuInfo = parseMenuInfo((JSONObject)chunghyang.get(chunghyangMenu[i]));
			//chunghyangMenuArr[i] = String.format("[%s] %s", chunghyangMenu[i], getOneline(menuInfo));
			chunghyangMenuArr[i] = String.format("- %s", getOneline(menuInfo));
		}
		
		return chunghyangMenuArr;
	}
	
	/**
	 * 메뉴와 가격 추출
	 * @param eachMenu
	 * @return
	 */
	private String[] parseMenuInfo(JSONObject eachMenu){
		String menuInfo[] = new String[2]; //메뉴, 가격
		menuInfo[0] = (String)eachMenu.get("메뉴");
		menuInfo[1] = (String)eachMenu.get("가격");
		return menuInfo;
	}
	
	/**
	 * 메뉴와 가격을 적절히 공백내용 제거 등을 통해 한줄로 만듬
	 * @param menuInfo
	 * @return
	 */
	private String getOneline(String[] menuInfo){
		String res = "";
		boolean containNumber = false;
		if(menuInfo[0]!=null && menuInfo[0].equals("")==false){
			res+=menuInfo[0];
		}
		if(menuInfo[1]!=null && menuInfo[1].equals("")==false) {
			for(int i=0;i<10;i++){
				if(menuInfo[1].contains(i+"")){
					containNumber = true;
					break;
				}
			}
			res+=" : "+(containNumber ? "￦" : "")+menuInfo[1];
		}
		return res;
	}
	
	/**
	 * 쓸모없는 HTML태그와 Escape Sequence를 제거
	 * @param str 태그 등을 제거할 문자열
	 * @return 태그와 이스케이프 시퀸스가 제거된 문자열
	 */
	private String replace(String str){
		return str.replace("\\r", "").replace("\\n", "").replace("<br>", " ");
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	public String getCarteByTime(){
		/* 파싱 실패시 */
		if(isSuccess == false) return "Parsing Fail";
		
		StringBuilder res = new StringBuilder();
		
		int i;
		menuByTime = new LinkedList[3];//아침, 점심, 저녁
		for(i=0;i<3;i++) menuByTime[i] = new LinkedList<>();

		menuByTime[0].add("\n********** 아침 메뉴 **********");
		menuByTime[1].add("\n********** 점심 메뉴 **********");
		menuByTime[2].add("\n********** 저녁 메뉴 **********");
		
		String[] haksik, bubsik, chiway, faculty, chunghyang;
		haksik = parseHaksik(allMenu);
		bubsik = parseBubsik(allMenu);
		//chiway = parseChiway(allMenu); //차이웨이는 출력에서 제외
		faculty = parseFaculty(allMenu);
		chunghyang = parseChunghyang(allMenu);

		boolean isPrinted[] = new boolean[3]; //아침 점심 저녁이 찍힌 적이 있는지 없는지(학식, 법식 등 태그 달기 위한 것)
		
		isPrinted[0] = isPrinted[1] = isPrinted[2] = false;
		/*
		 * 학식의 경우
		 * 0번째 : 아침
		 * 1~5번째 : 점심
		 * 6~8번째 : 저녁
		 */
		for(i=0;i<haksik.length;i++){
			if(i==0){
				if(isPrinted[0]==false){
					menuByTime[0].add("\n<< 학식 >>");
					isPrinted[0] = true;
				}
				menuByTime[0].add(haksik[i]);
			}
			else if(i<=5){
				if(isPrinted[1]==false){
					menuByTime[1].add("\n<< 학식 >>");
					isPrinted[1] = true;
				}
				menuByTime[1].add(haksik[i]);
			}
			else{
				if(isPrinted[2]==false){
					menuByTime[2].add("\n<< 학식 >>");
					isPrinted[2] = true;
				}
				menuByTime[2].add(haksik[i]);
			}
		}
		
		isPrinted[0] = isPrinted[1] = isPrinted[2] = false;
		/*
		 * 법식의 경우
		 * 0번째 : 아침
		 * 나머지는 중석식, 중식, 석식별로 걸러냄
		 */
		for(i=0;i<bubsik.length;i++){
			if(i==0){
				if(isPrinted[0]==false){
					menuByTime[0].add("\n<< 법식 >>");
					isPrinted[0] = true;
				}
				menuByTime[0].add(bubsik[i]);
			}
			else{
				if(bubsik[i].contains("중석식")){
					if(isPrinted[1]==false){
						menuByTime[1].add("\n<< 법식 >>");
						isPrinted[1] = true;
					}
					menuByTime[1].add(bubsik[i].replace("중석식", "").replace("*", ""));
					
					if(isPrinted[2]==false){
						menuByTime[2].add("\n<< 법식 >>");
						isPrinted[2] = true;
					}
					menuByTime[2].add(bubsik[i].replace("중석식", "").replace("*", ""));
				}
				else if(bubsik[i].contains("중식")){
					if(isPrinted[1]==false){
						menuByTime[1].add("\n<< 법식 >>");
						isPrinted[1] = true;
					}
					menuByTime[1].add(bubsik[i].replace("중식", "").replace("*", ""));
				}
				else if(bubsik[i].contains("석식")){
					if(isPrinted[2]==false){
						menuByTime[2].add("\n<< 법식 >>");
						isPrinted[2] = true;
					}
					menuByTime[2].add(bubsik[i].replace("석식", "").replace("*", ""));
				}
			}
		}
		
		/* 차이웨이는 중석식 다함
		isPrinted[0] = isPrinted[1] = isPrinted[2] = false;
		 
		for(i=0;i<chiway.length;i++){
			if(isPrinted[1]==false){
				menuByTime[1].add("\n<< 차이웨이 >>");
				isPrinted[1] = true;
			}
			menuByTime[1].add(chiway[i]);
			
			if(isPrinted[2]==false){
				menuByTime[2].add("\n<< 차이웨이 >>");
				isPrinted[2] = true;
			}
			menuByTime[2].add(chiway[i]);
		}
		*/
		
		isPrinted[0] = isPrinted[1] = isPrinted[2] = false;
		/*
		 * 교직원식당은
		 * 0~2 : 점심
		 * 3 : 저녁
		 */
		for(i=0;i<faculty.length;i++){
			if(i==3){
				if(isPrinted[2]==false){
					menuByTime[2].add("\n<< 교직원식당 >>");
					isPrinted[2] = true;
				}
				menuByTime[2].add(faculty[i]);
			}
			else{
				if(isPrinted[1]==false){
					menuByTime[1].add("\n<< 교직원식당 >>");
					isPrinted[1] = true;
				}
				menuByTime[1].add(faculty[i]);
			}
			
		}

		isPrinted[0] = isPrinted[1] = isPrinted[2] = false;
		/*
		 * 청향은 중석식 다함
		 */
		for(i=0;i<chunghyang.length;i++){
			if(isPrinted[1]==false){
				menuByTime[1].add("\n<< 청향 >>");
				isPrinted[1] = true;
			}
			menuByTime[1].add(chunghyang[i]);
			
			if(isPrinted[2]==false){
				menuByTime[2].add("\n<< 청향 >>");
				isPrinted[2] = true;
			}
			menuByTime[2].add(chunghyang[i]);
		}
		
		res.append(String.format("### 국민대 식단표(%s) ###\n", today));
		for(i=0;i<3;i++){
			for(String line : menuByTime[i]){
				if(line.length()>3) 
					res.append(line+"\n");
			}
		}
		return res.toString();
	}
}