package com.zhang.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.zhang.utils.HttpUtils;
import com.zhang.utils.utils;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Null;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.Thread.sleep;

@Component
public class StartPingService implements CommandLineRunner {
    int num = 0;
    int m = 0;
    int deCount = 0;
    private JSONObject submitDatabs = JSONObject.parseObject(utils.txt2String(new File("D:/spider/submitDatabs.txt")));
//    private JSONArray hostInfo = JSONArray.parseArray(utils.txt2String(new File("D:/spider/hostInfo.txt")));
    private JSONObject sailingProduct = null;
    private JSONObject listObject = null;
    private List<Map<String, String>> CookieList = utils.createCookieList("userInfo.json");
    private Map<String, String> mainAccount = CookieList.get(0);
    private String info = null;
    private String finResult = "";
    File file = new File("D:\\spider\\productInfo.txt");
    private JSONObject askForMap = JSONObject.parseObject(utils.txt2String(file));

    @Override
    public void run(String... args) throws Exception {
        while (true) {
            if (askForMap.getInteger("satisfyNum") > 0) {
                try {
                    for (int i = 0; i < CookieList.size(); i++) {
                        if (deCount >= CookieList.size()) {
                            deCount = 0;
                        }
                        sailingProduct = getPrebooking(CookieList.get(deCount));
                        deCount++;
                        if (sailingProduct != null) {
                            break;
                        } else {
                            listObject = null;
                        }
                        sleep(1000);
                    }
                } catch (Exception e) {
                    //????????????
                    sailingProduct = null;
                    e.printStackTrace();
                    deCount++;
                    System.out.println(deCount);
                    sleep(1000);
                }
                if (sailingProduct != null) {
                    //?????????????????????????????????????????????????????????????????????????????????
                    try {
                        for (int p = 0; p < askForMap.getJSONObject(info).getInteger("count"); p++) {
//                            System.out.println(1);
                            getSearchFeeData(sailingProduct);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw, true);
                        e.printStackTrace(pw);
                        pw.flush();
                        sw.flush();
                        utils.mail(sw.toString());
                    } finally {
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        fileOutputStream.write(askForMap.toString().getBytes(StandardCharsets.UTF_8));
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        sailingProduct = null;
                        info = null;
                    }
                }
            } else {
                break;
            }
        }
        //????????????????????????
        utils.mail(finResult);
        finResult = "";
    }

    //?????????????????????????????????
    public JSONObject getPrebooking(Map<String, String> params) throws Exception {
        Date date1 = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss:SSS");
        System.out.println(simpleDateFormat.format(new Date()));
        String url = "https://synconhub.coscoshipping.com/api/product/client/sailing/product/list";
        params.put("Referer", "https://synconhub.coscoshipping.com/product?channelCode=GENERAL&prodType=I&startDate=2021-12-22 00:00&cargoCategory=GENERAL&page=1");
        Map<String, Object> resultMap = submitDatabs;
        JSONArray contentArr = null;
        int contentArrSize = 0;
        if (listObject == null) {
//            System.out.println(hostInfo.getJSONObject(num));
            String result = HttpUtils.sendPost(url, resultMap, params);
            listObject = JSONObject.parseObject(result);
//            System.out.println("----------??????????????????????????????list??????");
//            System.out.println("listObj: " + listObject);
            contentArr = listObject.getJSONArray("content");
            contentArrSize = contentArr.size();
        }
//                    System.out.println(contentArr);
        //?????????????????????????????????
        for (int i = 0; i < contentArrSize; i++) {
            int flag = 0;
            JSONObject jsonObject = contentArr.getJSONObject(i);
            //??????????????????????????????????????????????????????0??????????????????????????????
            if (askForMap.getInteger("satisfyNum") > 0) {
                //????????????????????????
                askForMap.put("Boolean", false);
                //??????????????????>???????????????????????????????????????
                if (jsonObject.getInteger("inventory") >= askForMap.getInteger("wantNum") * utils.caculateTotal(askForMap)) {
//                        System.out.println(1);
                    info = jsonObject.getString("vesselName") + jsonObject.get("voyageNo").toString();
                    //???????????????????????????????????????
                    for (Map.Entry<String, Object> entry : askForMap.entrySet()) {
                        if (jsonObject.getJSONObject("containerOceanFeeMap").containsKey(entry.getKey()) && jsonObject.getJSONObject("containerOceanFeeMap").getInteger(entry.getKey()) <= Integer.parseInt(JSONObject.parseObject(entry.getValue().toString()).get("price").toString())) {
                            flag = flag + 1;
                        }
                    }
                    if (flag == utils.caculateInfo(askForMap)) {
                        //?????????????????????????????????
                        if (askForMap.containsKey(info)) {
                            //??????????????????
                            if (askForMap.getJSONObject(info).getInteger("count") > 0 && !askForMap.getJSONObject(info).getBoolean("did")) {
                                System.out.println(jsonObject.get("vesselName"));
                                System.out.println(jsonObject.get("voyageNo"));
                                System.out.println("----------??????????????????");
                            }
                            //????????????????????????-1
                            else if (!askForMap.getJSONObject(info).getBoolean("did")) {
                                askForMap.put("satisfyNum", askForMap.getInteger("satisfyNum") - 1);
                                askForMap.getJSONObject(info).put("did", true);
                                System.out.println("?????????????????????");
                            } else {
                                continue;
                            }
                        } else {
//                            continue;
                            JSONObject object = new JSONObject();
                            object.put("count", askForMap.getInteger("wantNum"));
                            object.put("did", false);
                            askForMap.put(info, object);
//                                System.out.println(object);
//                                System.out.println(askForMap);
                            System.out.println("????????????????????????");
                            System.out.println(jsonObject.get("vesselName"));
                            System.out.println(jsonObject.get("voyageNo"));
                            System.out.println("----------??????????????????");
                        }
                        System.out.println(jsonObject);
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        System.out.println("??????????????????>>" + df.format(new Date()));
                        //                                sleep((int) Math.round((Math.random() * 3) + 2) * 1000); //????????????
                        return jsonObject;
                    }
                }
            } else if (!askForMap.getBoolean("Boolean")) {
                askForMap.put("Boolean", true);
            } else {
                continue;
            }
        }
        Date date2 = new Date();
        if ((date2.getTime() - date1.getTime()) > 3000) {
            m++;
            System.out.println("????????????:" + m);
        }
        System.out.println("????????????????????????");
        return null;
    }

    private void getSearchFeeData(JSONObject sailingProduct) throws Exception {
        JSONObject searchFeeObj = new JSONObject();
        searchFeeObj.put("key", 1);
        searchFeeObj.put("chargeTag", "POR");
        searchFeeObj.put("effectiveTime", utils.formatUTC(sailingProduct.getString("ltd") + ":00"));
        searchFeeObj.put("chargeModel", "BOTH");
        searchFeeObj.put("tradeAreaCode", sailingProduct.getString("tradeArea"));
        searchFeeObj.put("tradeLaneCode", sailingProduct.getString("tradeLaneCode"));
        searchFeeObj.put("svcLoopCode", sailingProduct.getString("serviceCode"));
        searchFeeObj.put("direction", sailingProduct.getString("direction"));
        searchFeeObj.put("areaCode", sailingProduct.getString("areaCode"));
        searchFeeObj.put("regionCode", sailingProduct.getString("regionCode"));
        searchFeeObj.put("por", sailingProduct.getJSONObject("porCity").getString("id"));
        searchFeeObj.put("pod", sailingProduct.getJSONObject("podPort").getString("id"));
        searchFeeObj.put("fnd", sailingProduct.getJSONObject("fndCity").getString("id"));
        searchFeeObj.put("channelCode", sailingProduct.getString("channelCode"));
        searchFeeObj.put("cargoCategory", sailingProduct.getString("cargoType"));
        searchFeeObj.put("paymentTerms", "P");
        searchFeeObj.put("isPreBookingOrder", false);

        String cntrInfoStr = "[{\"cntrSize\":\"40HQ\",\"amount\":1},{\"cntrSize\":\"20GP\",\"amount\":1},{\"cntrSize\":\"40GP\",\"amount\":1}]";
        JSONArray cntrInfoArr = JSONArray.parseArray(cntrInfoStr);

        searchFeeObj.put("cntrInfo", cntrInfoArr);
        searchFeeObj.put("transhipmentPortIds", new JSONArray());

        System.out.println("----------??????????????????????????????Request?????????");
        System.out.println("searchFeeObj: " + searchFeeObj);

        postSearchFee(searchFeeObj, sailingProduct);
    }

    private void postSearchFee(JSONObject searchFeeObj, JSONObject sailingProduct) throws Exception {
        String url = "https://synconhub.coscoshipping.com/api/common/feeUpload/searchfee";

        JSONArray paramArr = new JSONArray();
        paramArr.add(searchFeeObj);
        mainAccount.put("Referer", "https://synconhub.coscoshipping.com/product/order/instant/detail/" + sailingProduct.get("id"));

        String resultSearchFee = HttpUtils.sendPostArray(url, paramArr, mainAccount);

        JSONArray searchFeeArr = JSONArray.parseArray(resultSearchFee);
        System.out.println("----------??????searchfee?????????????????????");
        System.out.println("searchFeeArr: " + searchFeeArr);
        JSONObject detailObj = searchFeeArr.getJSONObject(0);
        //?????????????????????????????????
        JSONArray chargeInfoArr = detailObj.getJSONArray("chargeInfo");
        JSONArray orderItems = new JSONArray();
        JSONArray chargeDetailArr = new JSONArray();
        for (Map.Entry<String, Object> entry : askForMap.entrySet()) {
            for (int i = 0; i < chargeInfoArr.size(); i++) {
                JSONArray orderItemCharges = new JSONArray();
                JSONObject orderItemObj = new JSONObject();
                JSONArray oceanFeeArray = sailingProduct.getJSONArray("routeProductPricingList");
                if (chargeInfoArr.getJSONObject(i).containsValue(entry.getKey())) {
                    System.out.println("????????????" + entry.getKey() + "?????????");
                    JSONObject chargeInfoObj = chargeInfoArr.getJSONObject(i);
                    chargeDetailArr = chargeInfoObj.getJSONArray("chargeDetail");
                    System.out.println("----------?????????????????????????????????chargeDetail????????????");
                    System.out.println("chargeDetailArr: " + chargeDetailArr);
                    //??????????????????20GP  40GP  40HQ ????????????????????????
                    //1.???????????????
                    for (int j = 0; j < oceanFeeArray.size(); j++) {
                        if (oceanFeeArray.getJSONObject(j).containsValue(entry.getKey())) {
                            JSONObject oceanFeeTmpObj = oceanFeeArray.getJSONObject(j).getJSONArray("sailingProductPricingDetailList").getJSONObject(0);
                            oceanFeeTmpObj.put("chargeName", "?????????");
                            oceanFeeTmpObj.put("chargeCode", null);
                            oceanFeeTmpObj.put("toCurrency", "CNY");
                            oceanFeeTmpObj.put("transitPortId", null);
                            oceanFeeTmpObj.put("paymentTermsType", "P");
                            oceanFeeTmpObj.remove("id");
                            oceanFeeTmpObj.remove("cntrType");
                            orderItemCharges.add(oceanFeeTmpObj);
                            System.out.println("----------???????????????????????????");
                            System.out.println("oceanFeeTmpObj: " + oceanFeeTmpObj);
                        }
                    }
                    //2.??????????????????????????????
                    if (chargeInfoArr.getJSONObject(i).containsValue("CNTR")) {
                        for (int n = 0; n < chargeDetailArr.size(); n++) {
                            if (chargeDetailArr.getJSONObject(n).containsValue("P")) {
                                chargeDetailArr.getJSONObject(n).remove("category");
                                chargeDetailArr.getJSONObject(n).remove("chargeTag");
                                chargeDetailArr.getJSONObject(n).remove("isFollowOceanFee");
                                chargeDetailArr.getJSONObject(n).remove("transhipmentPortId");
                                chargeDetailArr.getJSONObject(n).put("toCurrency", "CNY");
                                chargeDetailArr.getJSONObject(n).put("transitPortId", null);
                                chargeDetailArr.getJSONObject(n).put("paymentTermsType", chargeDetailArr.getJSONObject(n).get("paymentTerms"));
                                chargeDetailArr.getJSONObject(n).remove("paymentTerms");
                                orderItemCharges.add(chargeDetailArr.getJSONObject(n));
                            }
                        }
                        orderItemCharges.add(JSONObject.parseObject("{\n" +
                                "  \"chargeName\": \"????????????\",\n" +
                                "  \"chargeCode\": null,\n" +
                                "  \"chargeType\": \"INSURED\",\n" +
                                "  \"price\": 5,\n" +
                                "  \"currency\": \"USD\",\n" +
                                "  \"toCurrency\": \"CNY\",\n" +
                                "  \"transitPortId\": null,\n" +
                                "  \"paymentTermsType\": \"P\"\n" +
                                "}"));
                        System.out.println("----------????????????????????????????????????????????????");
                        System.out.println("orderItemCharges: " + orderItemCharges);
                        orderItemObj.put("cntrType", entry.getKey());
                        orderItemObj.put("itemType", "CNTR");
                        orderItemObj.put("quantity", JSONObject.parseObject(entry.getValue().toString()).getInteger("eachNum"));
                        orderItemObj.put("orderItemCharges", orderItemCharges);
                        orderItems.add(orderItemObj);
                    }
                }
            }
        }
        //??????????????????BL??????????????????
        for (int i = 0; i < chargeInfoArr.size(); i++) {
            JSONArray orderItemChargeBL = new JSONArray();
            JSONObject orderItemBLObj = new JSONObject();
            if (chargeInfoArr.getJSONObject(i).containsValue("BL")) {
                System.out.println("????????????BL??????");
                JSONObject chargeInfoObjBL = chargeInfoArr.getJSONObject(i);
                JSONArray chargeDetailArrBL = chargeInfoObjBL.getJSONArray("chargeDetail");
                //??????BL??????
                for (int k = 0; k < chargeDetailArrBL.size(); k++) {
                    if (chargeDetailArrBL.getJSONObject(k).containsValue("P")) {
                        chargeDetailArrBL.getJSONObject(k).remove("category");
                        chargeDetailArrBL.getJSONObject(k).remove("chargeTag");
                        chargeDetailArrBL.getJSONObject(k).remove("isFollowOceanFee");
                        chargeDetailArrBL.getJSONObject(k).remove("transhipmentPortId");
                        chargeDetailArrBL.getJSONObject(k).put("toCurrency", chargeDetailArrBL.getJSONObject(k).get("currency"));
                        chargeDetailArrBL.getJSONObject(k).put("transitPortId", null);
                        chargeDetailArrBL.getJSONObject(k).put("paymentTermsType", chargeDetailArrBL.getJSONObject(k).get("paymentTerms"));
                        chargeDetailArrBL.getJSONObject(k).remove("paymentTerms");
                        orderItemChargeBL.add(chargeDetailArrBL.getJSONObject(k));
                    }
                }
                orderItemBLObj.put("itemType", "BL");
                orderItemBLObj.put("quantity", 1);
                orderItemBLObj.put("orderItemCharges", orderItemChargeBL);
                orderItems.add(orderItemBLObj);
                System.out.println("----------??????BL??????????????????");
                System.out.println("orderItemBLObj: " + orderItemBLObj);
            }
        }
        System.out.println("----------??????20GP  40GP  40HQ ???????????????????????????");
        System.out.println("orderItems: " + orderItems);
        String getUrl = "https://synconhub.coscoshipping.com/api/product/client/sailing/product/";
        JSONObject order = new JSONObject();
        order.put("cargoCategory", searchFeeObj.get("cargoCategory"));
        order.put("channelCode", searchFeeObj.get("channelCode"));
        order.put("coupon", new JSONObject());
//        order.put("insuredRuleUuid","8aaa8dc16dab7d09016dabc031e20005");
        order.put("orderItems", orderItems);
        order.put("paymentInfo", new JSONObject());
        order.put("paymentPercentage", 5);
        order.put("prodId", sailingProduct.get("id"));
        order.put("prodType", "SAILING_PROD");
        order.put("reeferValueAddServices", new JSONArray());
        order.put("sailingProdType", "I");
        order.put("tradeLaneCode", searchFeeObj.get("tradeLaneCode"));
        order.put("trailerServices", new JSONArray());
        order.put("transferServices", new JSONArray());
        order.put("totalPricePercentage", 5);
//        System.out.println(order);
        JSONObject orderSub = new JSONObject();
//        orderSub.put("fmcBookingParam",new JSONObject());
        orderSub.put("operations", new JSONArray());
        orderSub.put("order", order);
        orderSub.put("orderPromotionUsageParam", new JSONArray());
        postOrderData(orderSub);
    }

    private void postOrderData(JSONObject orderSub) throws Exception {

        String url = "https://synconhub.coscoshipping.com/api/product/client/order";
        String getUrl = "https://synconhub.coscoshipping.com/api/product/client/sailing/product/";
        mainAccount.put("Referer", "https://synconhub.coscoshipping.com/product/order/preview");

        String isSuredRuler = HttpUtils.sendGet(getUrl + orderSub.getJSONObject("order").get("prodId"), mainAccount);
        JSONObject object = JSONObject.parseObject(isSuredRuler);
        orderSub.getJSONObject("order").put("insuredRuleUuid", object.getJSONObject("insuredRule").get("id").toString());
        System.out.println("----------??????order??????????????????????????????");
        System.out.println("orderSub: " + orderSub);
        Map<String, Object> paramMap = JSONObject.parseObject(orderSub.toJSONString(), new TypeReference<Map<String, Object>>() {
        });
//        System.out.println(paramMap);
        String resultOrder = HttpUtils.sendPost(url, paramMap, mainAccount);
        System.out.println("----------???????????????????????????????????????id?????????");
        System.out.println(resultOrder);
        JSONObject resultOrderObj = JSONObject.parseObject(resultOrder);
        getPaymentData(resultOrderObj);
        System.out.println(new Date());
    }

    private void getPaymentData(JSONObject resultOrderObj) throws Exception {
        String url = "https://synconhub.coscoshipping.com/api/product/client/order-payment/detail?productOrderType=SAILING_PROD&orderNo=" + resultOrderObj.get("orderNo").toString();


        mainAccount.put("Referer", "https://synconhub.coscoshipping.com/order/unpaidTotalCharge?orderNo=" + resultOrderObj.get("orderNo").toString());

        String result = HttpUtils.sendGet(url, mainAccount);
//        System.out.println("---------HttpUtils.sendGet(url,params)-----------");
//        System.out.println(result);
        JSONObject object = JSONObject.parseObject(result);
        JSONObject sailingProdOrderInfo = new JSONObject();
        JSONObject accountInfo = new JSONObject();
        JSONObject accountInfoSubObj = new JSONObject();

        sailingProdOrderInfo.put("amount", object.getJSONObject("SAILING_PROD").get("amount"));
        sailingProdOrderInfo.put("orderNo", object.getJSONObject("SAILING_PROD").get("orderNo"));
        sailingProdOrderInfo.put("paymentOrderType", object.getJSONObject("SAILING_PROD").get("orderType"));

        accountInfo.put("payWay", "PREPAID");
        //????????????
//        accountInfo.put("password", "987654");
        //??????
        accountInfo.put("password", "DwTnNQXl7tFC930zkFvUNnNtfI9A5A1xleHbYpmQxb58/wAmuC7v0Rw6CecJo9fUON+n7P4TAasKFqPtQTVVqXpOABqSbEJ50Wlt7O0pInlqRxtHPQbsO3E3Nqg+khD48jXtfg+3aHNI7YTAs5CDZRwBcoNvwwPtpX54ayEXM2S+WDUjVmVaIoFGVXECUo4MAYEdQjCU3+9zX3ijYpESkmHvvkUl84mCUwmHpfv9yQ+1fEIEdTj8lq1+QVL2pe1f7hsGa2Yg62GROUPcM4JxEdpWy2MeJWYSNF6kCuhv4VJ7QXzfnysj7JOAc6sBzbGmZYh4pVkcnw5sCqVZYw4IuA==");

        accountInfo.put("unionPayType", "COMPANY");
        accountInfo.put("scac", "COSU");

        accountInfoSubObj.put("sailingProdOrderInfo", sailingProdOrderInfo);
        accountInfoSubObj.put("accountInfo", accountInfo);

        System.out.println("----------?????????????????????????????????Request??????");
        System.out.println(accountInfoSubObj);
        putOrderPayment(accountInfoSubObj);
    }

    private void putOrderPayment(JSONObject accountInfoSubObj) throws Exception {

        String url = "https://synconhub.coscoshipping.com/api/product/client/order-payment/pay";
        mainAccount.put("Referer", "https://synconhub.coscoshipping.com/order/unpaidTotalCharge?orderNo=" + accountInfoSubObj.getJSONObject("sailingProdOrderInfo").get("orderNo"));
        Map<String, Object> paramMap = JSONObject.parseObject(accountInfoSubObj.toJSONString(), new TypeReference<Map<String, Object>>() {
        });
//        System.out.println("---------JSONObject paramMap = accountInfoSubObj---------");
//        System.out.println(paramMap);
        String resultPayment = HttpUtils.sendPut(url, paramMap, mainAccount);
        System.out.println("-----------????????????????????????-------------");
        System.out.println(resultPayment);
        num = num + 1;
        if (HttpUtils.putCode == 200) {
            finResult = finResult + "???" + num + "?????????" + "???????????????" + resultPayment + "\n";
            askForMap.getJSONObject(info).put("count", askForMap.getJSONObject(info).getInteger("count") - 1);
            System.out.println("?????????????????????????????????");
        } else {
            finResult = finResult + "???" + num + "?????????" + "???????????????" + resultPayment + "\n";
            if (!askForMap.getJSONObject(info).getBoolean("did")) {
                askForMap.put("satisfyNum", askForMap.getInteger("satisfyNum") - 1);
            }
            askForMap.getJSONObject(info).put("did", true);
            System.out.println("?????????????????????????????????");
        }
        HttpUtils.putCode = 0;
    }
}

