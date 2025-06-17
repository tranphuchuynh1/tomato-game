package com.raindrop.game.payment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class VNPayHelper {
    // VNPay Configuration
    private static final String VNP_TMN_CODE = "4SOXLFSO";
    private static final String VNP_HASH_SECRET = "EP919YO96XL7HKF4WMG6714LV277SHSO";
    private static final String VNP_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String VNP_RETURN_URL = "vnpay://payment/result";

    // Payment callback interface
    public interface PaymentCallback {
        void onPaymentSuccess(String transactionId, String orderId);
        void onPaymentFailure(String errorMessage);
        void onPaymentCancelled();
    }

    private static PaymentCallback currentCallback;
    private static PaymentPlatform paymentPlatform;

    /**
     * Khởi tạo với platform implementation
     */
    public static void initialize(PaymentPlatform platform) {
        paymentPlatform = platform;
    }

    /**
     * Validate và format IP address thành IPv4
     */
    private static String validateIPv4(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return "127.0.0.1";
        }

        // Loại bỏ IPv6 mapped IPv4
        if (ip.startsWith("::ffff:")) {
            ip = ip.substring(7);
        }

        // Kiểm tra format IPv4 cơ bản
        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            try {
                for (String part : parts) {
                    int num = Integer.parseInt(part);
                    if (num < 0 || num > 255) {
                        return "127.0.0.1";
                    }
                }
                return ip;
            } catch (NumberFormatException e) {
                return "127.0.0.1";
            }
        }

        return "127.0.0.1";
    }

    private static String sanitizeOrderInfo(String orderInfo) {
        if (orderInfo == null) {
            return "";
        }

        // Thay thế các ký tự có dấu thành không dấu
        String sanitized = orderInfo
            .replace("à", "a").replace("á", "a").replace("ả", "a").replace("ã", "a").replace("ạ", "a")
            .replace("ă", "a").replace("ằ", "a").replace("ắ", "a").replace("ẳ", "a").replace("ẵ", "a").replace("ặ", "a")
            .replace("â", "a").replace("ầ", "a").replace("ấ", "a").replace("ẩ", "a").replace("ẫ", "a").replace("ậ", "a")
            .replace("è", "e").replace("é", "e").replace("ẻ", "e").replace("ẽ", "e").replace("ẹ", "e")
            .replace("ê", "e").replace("ề", "e").replace("ế", "e").replace("ể", "e").replace("ễ", "e").replace("ệ", "e")
            .replace("ì", "i").replace("í", "i").replace("ỉ", "i").replace("ĩ", "i").replace("ị", "i")
            .replace("ò", "o").replace("ó", "o").replace("ỏ", "o").replace("õ", "o").replace("ọ", "o")
            .replace("ô", "o").replace("ồ", "o").replace("ố", "o").replace("ổ", "o").replace("ỗ", "o").replace("ộ", "o")
            .replace("ơ", "o").replace("ờ", "o").replace("ớ", "o").replace("ở", "o").replace("ỡ", "o").replace("ợ", "o")
            .replace("ù", "u").replace("ú", "u").replace("ủ", "u").replace("ũ", "u").replace("ụ", "u")
            .replace("ư", "u").replace("ừ", "u").replace("ứ", "u").replace("ử", "u").replace("ữ", "u").replace("ự", "u")
            .replace("ỳ", "y").replace("ý", "y").replace("ỷ", "y").replace("ỹ", "y").replace("ỵ", "y")
            .replace("đ", "d")
            .replace("À", "A").replace("Á", "A").replace("Ả", "A").replace("Ã", "A").replace("Ạ", "A")
            .replace("Ă", "A").replace("Ằ", "A").replace("Ắ", "A").replace("Ẳ", "A").replace("Ẵ", "A").replace("Ặ", "A")
            .replace("Â", "A").replace("Ầ", "A").replace("Ấ", "A").replace("Ẩ", "A").replace("Ẫ", "A").replace("Ậ", "A")
            .replace("È", "E").replace("É", "E").replace("Ẻ", "E").replace("Ẽ", "E").replace("Ẹ", "E")
            .replace("Ê", "E").replace("Ề", "E").replace("Ế", "E").replace("Ể", "E").replace("Ễ", "E").replace("Ệ", "E")
            .replace("Ì", "I").replace("Í", "I").replace("Ỉ", "I").replace("Ĩ", "I").replace("Ị", "I")
            .replace("Ò", "O").replace("Ó", "O").replace("Ỏ", "O").replace("Õ", "O").replace("Ọ", "O")
            .replace("Ô", "O").replace("Ồ", "O").replace("Ố", "O").replace("Ổ", "O").replace("Ỗ", "O").replace("Ộ", "O")
            .replace("Ơ", "O").replace("Ờ", "O").replace("Ớ", "O").replace("Ở", "O").replace("Ỡ", "O").replace("Ợ", "O")
            .replace("Ù", "U").replace("Ú", "U").replace("Ủ", "U").replace("Ũ", "U").replace("Ụ", "U")
            .replace("Ư", "U").replace("Ừ", "U").replace("Ứ", "U").replace("Ử", "U").replace("Ữ", "U").replace("Ự", "U")
            .replace("Ỳ", "Y").replace("Ý", "Y").replace("Ỷ", "Y").replace("Ỹ", "Y").replace("Ỵ", "Y")
            .replace("Đ", "D");

        return sanitized;
    }

    /**
     * Tạo URL thanh toán VNPay - SIGNATURE FIXED VERSION
     */
    public static String createPaymentUrl(String orderId, int amount, String orderInfo, String ipAddr) {
        try {
            // Validate IP address
            String validatedIP = validateIPv4(ipAddr);

            // Tạo tham số với đầy đủ các field bắt buộc
            Map<String, String> vnpParams = new HashMap<>();

            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", VNP_TMN_CODE);
            vnpParams.put("vnp_Amount", String.valueOf(amount * 100));
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", orderId);
            vnpParams.put("vnp_OrderInfo", sanitizeOrderInfo(orderInfo));
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", VNP_RETURN_URL);
            vnpParams.put("vnp_IpAddr", validatedIP);

            // Tạo thời gian với timezone Việt Nam
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

            String vnpCreateDate = formatter.format(calendar.getTime());
            vnpParams.put("vnp_CreateDate", vnpCreateDate);

            // Thời gian hết hạn (15 phút)
            calendar.add(Calendar.MINUTE, 15);
            String vnpExpireDate = formatter.format(calendar.getTime());
            vnpParams.put("vnp_ExpireDate", vnpExpireDate);

            // Sắp xếp tham số theo alphabetical order
            List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
            Collections.sort(fieldNames);

            // Build hash data và query string - KEY FIX: Cả hai đều sử dụng RAW values
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (int i = 0; i < fieldNames.size(); i++) {
                String fieldName = fieldNames.get(i);
                String fieldValue = vnpParams.get(fieldName);

                if (fieldValue != null && !fieldValue.isEmpty()) {
                    // FIXED: Cả hash data và query đều sử dụng RAW values
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(fieldValue);

                    // Query string cũng sử dụng RAW values
                    query.append(fieldName);
                    query.append('=');
                    query.append(fieldValue);

                    if (i < fieldNames.size() - 1) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            // Tạo secure hash từ RAW hashData
            String vnpSecureHash = hmacSHA512(VNP_HASH_SECRET, hashData.toString());
            if (vnpSecureHash == null) {
                System.err.println("Failed to generate secure hash");
                return null;
            }

            // FIXED: Encode toàn bộ query string cuối cùng để tránh lỗi Unicode
            String rawQueryUrl = query.toString() + "&vnp_SecureHash=" + vnpSecureHash;
            String encodedQueryUrl = encodeQueryString(rawQueryUrl);
            String paymentUrl = VNP_URL + "?" + encodedQueryUrl;

            // Debug logging
            System.out.println("=== VNPay Payment URL Debug (SIGNATURE FIXED) ===");
            System.out.println("Order ID: " + orderId);
            System.out.println("Amount: " + amount + " VND (" + (amount * 100) + " in VNPay format)");
            System.out.println("Order Info: " + orderInfo);
            System.out.println("IP Address (validated): " + validatedIP);
            System.out.println("Create Date: " + vnpCreateDate);
            System.out.println("Parameters count: " + vnpParams.size());
            System.out.println("Hash Data (RAW): " + hashData.toString());
            System.out.println("Secure Hash: " + vnpSecureHash);
            System.out.println("Raw Query: " + rawQueryUrl);
            System.out.println("Encoded Query: " + encodedQueryUrl);
            System.out.println("Final URL Length: " + paymentUrl.length());
            System.out.println("Final URL: " + paymentUrl);
            System.out.println("=============================================");

            return paymentUrl;

        } catch (Exception e) {
            System.err.println("Error creating payment URL: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Encode query string để xử lý Unicode an toàn
     */
    private static String encodeQueryString(String queryString) {
        try {
            String[] pairs = queryString.split("&");
            StringBuilder encodedQuery = new StringBuilder();

            for (int i = 0; i < pairs.length; i++) {
                String pair = pairs[i];
                String[] keyValue = pair.split("=", 2);

                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];

                    // Encode cả key và value
                    String encodedKey = URLEncoder.encode(key, "UTF-8");
                    String encodedValue = URLEncoder.encode(value, "UTF-8");

                    encodedQuery.append(encodedKey).append("=").append(encodedValue);
                } else {
                    // Key without value
                    encodedQuery.append(URLEncoder.encode(keyValue[0], "UTF-8"));
                }

                if (i < pairs.length - 1) {
                    encodedQuery.append("&");
                }
            }

            return encodedQuery.toString();

        } catch (UnsupportedEncodingException e) {
            System.err.println("Error encoding query string: " + e.getMessage());
            return queryString; // Fallback to original
        }
    }

    /**
     * Mở trang thanh toán - sử dụng platform interface
     */
    public static void openPayment(String orderId, int amount, String orderInfo, PaymentCallback callback) {
        if (paymentPlatform == null) {
            if (callback != null) {
                callback.onPaymentFailure("PaymentPlatform chưa được khởi tạo");
            }
            return;
        }

        // Validate input parameters
        if (orderId == null || orderId.trim().isEmpty()) {
            if (callback != null) {
                callback.onPaymentFailure("Order ID không được để trống");
            }
            return;
        }

        if (amount <= 0) {
            if (callback != null) {
                callback.onPaymentFailure("Số tiền phải lớn hơn 0");
            }
            return;
        }

        currentCallback = callback;

        try {
            String ipAddr = paymentPlatform.getDeviceIP();
            String paymentUrl = createPaymentUrl(orderId, amount, orderInfo, ipAddr);

            if (paymentUrl == null) {
                if (callback != null) {
                    callback.onPaymentFailure("Không thể tạo URL thanh toán");
                }
                return;
            }

            System.out.println("Opening payment URL: " + paymentUrl);
            paymentPlatform.openBrowser(paymentUrl);

        } catch (Exception e) {
            System.err.println("Error opening payment: " + e.getMessage());
            e.printStackTrace();
            if (callback != null) {
                callback.onPaymentFailure("Không thể mở trang thanh toán: " + e.getMessage());
            }
        }
    }

    /**
     * Xử lý kết quả thanh toán từ VNPay
     */
    public static void handlePaymentResult(Map<String, String> params) {
        try {
            System.out.println("=== VNPay Payment Result Debug ===");
            System.out.println("Received params count: " + params.size());

            // Log all received parameters (for debugging)
            for (Map.Entry<String, String> entry : params.entrySet()) {
                System.out.println("Param: " + entry.getKey() + " = " + entry.getValue());
            }

            String vnpResponseCode = params.get("vnp_ResponseCode");
            String vnpTransactionNo = params.get("vnp_TransactionNo");
            String vnpTxnRef = params.get("vnp_TxnRef");
            String vnpSecureHash = params.get("vnp_SecureHash");

            System.out.println("Response Code: " + vnpResponseCode);
            System.out.println("Transaction No: " + vnpTransactionNo);
            System.out.println("Txn Ref: " + vnpTxnRef);
            System.out.println("Received Hash: " + vnpSecureHash);

            // Kiểm tra có hash không
            if (vnpSecureHash == null || vnpSecureHash.trim().isEmpty()) {
                System.out.println("No secure hash received");
                if (currentCallback != null) {
                    currentCallback.onPaymentFailure("Không nhận được chữ ký bảo mật");
                }
                return;
            }

            // Xác thực chữ ký
            if (validateSignature(params)) {
                System.out.println("Signature validation: SUCCESS");

                if ("00".equals(vnpResponseCode)) {
                    // Thanh toán thành công
                    System.out.println("Payment result: SUCCESS");
                    if (currentCallback != null) {
                        currentCallback.onPaymentSuccess(vnpTransactionNo, vnpTxnRef);
                    }
                } else if ("24".equals(vnpResponseCode)) {
                    // Người dùng hủy giao dịch
                    System.out.println("Payment result: CANCELLED");
                    if (currentCallback != null) {
                        currentCallback.onPaymentCancelled();
                    }
                } else {
                    // Thanh toán thất bại
                    String errorMessage = getErrorMessage(vnpResponseCode);
                    System.out.println("Payment result: FAILED - " + errorMessage);
                    if (currentCallback != null) {
                        currentCallback.onPaymentFailure(errorMessage);
                    }
                }
            } else {
                // Chữ ký không hợp lệ
                System.out.println("Signature validation: FAILED");
                if (currentCallback != null) {
                    currentCallback.onPaymentFailure("Chữ ký không hợp lệ");
                }
            }
            System.out.println("===================================");

        } catch (Exception e) {
            System.err.println("Error handling payment result: " + e.getMessage());
            e.printStackTrace();
            if (currentCallback != null) {
                currentCallback.onPaymentFailure("Lỗi xử lý kết quả thanh toán: " + e.getMessage());
            }
        }
    }

    /**
     * Xác thực chữ ký từ VNPay - SIGNATURE VALIDATION FIXED
     */
    private static boolean validateSignature(Map<String, String> params) {
        try {
            String receivedHash = params.get("vnp_SecureHash");
            if (receivedHash == null || receivedHash.trim().isEmpty()) {
                System.out.println("Received hash is null or empty");
                return false;
            }

            // Loại bỏ secure hash và vnp_SecureHashType khỏi params
            Map<String, String> sortedParams = new TreeMap<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (!"vnp_SecureHash".equals(key) &&
                    !"vnp_SecureHashType".equals(key) &&
                    value != null &&
                    !value.trim().isEmpty()) {
                    sortedParams.put(key, value);
                }
            }

            System.out.println("Parameters for validation count: " + sortedParams.size());

            // Xây dựng hash data với RAW values (giống như khi tạo payment URL)
            List<String> fieldNames = new ArrayList<>(sortedParams.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();

            for (int i = 0; i < fieldNames.size(); i++) {
                String fieldName = fieldNames.get(i);
                String fieldValue = sortedParams.get(fieldName);

                // Sử dụng RAW values - không encode
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(fieldValue);

                if (i < fieldNames.size() - 1) {
                    hashData.append('&');
                }
            }

            // Tạo checksum
            String hashDataString = hashData.toString();
            String myChecksum = hmacSHA512(VNP_HASH_SECRET, hashDataString);

            // Debug logging
            System.out.println("\n=== SIGNATURE VALIDATION DEBUG (FIXED) ===");
            System.out.println("Sorted params count: " + sortedParams.size());
            System.out.println("Hash data for validation (RAW): " + hashDataString);
            System.out.println("Hash data length: " + hashDataString.length());
            System.out.println("My checksum: " + myChecksum);
            System.out.println("Received checksum: " + receivedHash);

            boolean isMatch = myChecksum != null && myChecksum.equalsIgnoreCase(receivedHash);
            System.out.println("Checksums match: " + isMatch);

            if (!isMatch) {
                System.out.println("SIGNATURE MISMATCH DETAILS:");
                System.out.println("Expected: " + myChecksum);
                System.out.println("Received: " + receivedHash);
                System.out.println("Hash secret used: " + VNP_HASH_SECRET);
                System.out.println("TMN Code used: " + VNP_TMN_CODE);
            }

            System.out.println("==============================================\n");

            return isMatch;

        } catch (Exception e) {
            System.err.println("Error validating signature: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tạo HMAC SHA512 - VERIFIED CORRECT
     */
    private static String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                System.err.println("HMAC SHA512: Key or data is null");
                return null;
            }

            if (key.trim().isEmpty() || data.trim().isEmpty()) {
                System.err.println("HMAC SHA512: Key or data is empty");
                return null;
            }

            javax.crypto.Mac hmac512 = javax.crypto.Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);

            // Convert to hex string (lowercase)
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }

            String hash = sb.toString();
            System.out.println("Generated HMAC SHA512 for data length " + data.length() + ": " + hash.substring(0, Math.min(20, hash.length())) + "...");
            return hash;

        } catch (Exception e) {
            System.err.println("Error generating HMAC SHA512: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Handle URL parameters that might be encoded - IMPROVED VERSION
     */
    public static void handlePaymentResultFromURL(String returnUrl) {
        try {
            System.out.println("Processing return URL: " + returnUrl);
            Map<String, String> params = parseUrlParameters(returnUrl);
            System.out.println("Parsed " + params.size() + " parameters from URL");
            handlePaymentResult(params);
        } catch (Exception e) {
            System.err.println("Error parsing URL parameters: " + e.getMessage());
            e.printStackTrace();
            if (currentCallback != null) {
                currentCallback.onPaymentFailure("Lỗi phân tích URL trả về: " + e.getMessage());
            }
        }
    }

    /**
     * Parse URL parameters properly handling encoding - IMPROVED VERSION
     */
    private static Map<String, String> parseUrlParameters(String url) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<>();

        if (url == null || !url.contains("?")) {
            System.out.println("URL is null or contains no query parameters");
            return params;
        }

        String queryString = url.substring(url.indexOf("?") + 1);
        System.out.println("Query string: " + queryString);

        String[] pairs = queryString.split("&");
        System.out.println("Found " + pairs.length + " parameter pairs");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = URLDecoder.decode(keyValue[0], "UTF-8");
                String value = URLDecoder.decode(keyValue[1], "UTF-8");
                params.put(key, value);

                // Debug log
                System.out.println("Parsed param: " + key + " = " + value);
            } else if (keyValue.length == 1) {
                // Parameter without value
                String key = URLDecoder.decode(keyValue[0], "UTF-8");
                params.put(key, "");
                System.out.println("Parsed param (no value): " + key + " = ''");
            }
        }

        return params;
    }

    /**
     * Lấy thông báo lỗi từ mã lỗi VNPay
     */
    private static String getErrorMessage(String responseCode) {
        switch (responseCode) {
            case "01": return "Giao dịch chưa hoàn tất";
            case "02": return "Giao dịch bị lỗi";
            case "04": return "Giao dịch đảo (Khách hàng đã bị trừ tiền tại Ngân hàng nhưng GD chưa thành công ở VNPAY)";
            case "05": return "VNPAY đang xử lý giao dịch này (GD hoàn tiền)";
            case "06": return "VNPAY đã gửi yêu cầu hoàn tiền sang Ngân hàng (GD hoàn tiền)";
            case "07": return "Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)";
            case "09": return "GD Hoàn trả bị từ chối";
            case "10": return "Đã giao hàng";
            case "11": return "Giao dịch không được phép";
            case "12": return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng.";
            case "13": return "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP).";
            case "24": return "Giao dịch không thành công do: Khách hàng hủy giao dịch";
            case "51": return "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.";
            case "65": return "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày.";
            case "75": return "Ngân hàng thanh toán đang bảo trì.";
            case "79": return "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định.";
            case "99": return "Các lỗi khác (lỗi còn lại, không có trong danh sách mã lỗi đã liệt kê)";
            default: return "Giao dịch thất bại với mã lỗi: " + responseCode;
        }
    }

    /**
     * Tạo mã đơn hàng duy nhất - IMPROVED VERSION
     */
    public static String generateOrderId(String itemId) {
        // Đảm bảo orderId duy nhất và không trùng lặp
        // Giới hạn độ dài để tránh vượt quá giới hạn VNPay
        String cleanItemId = itemId.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (cleanItemId.length() > 10) {
            cleanItemId = cleanItemId.substring(0, 10);
        }

        return "ORDER_" + cleanItemId + "_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    /**
     * Định dạng số tiền VND
     */
    public static String formatVND(int amount) {
        return String.format("%,d", amount) + "đ";
    }

    /**
     * Kiểm tra tính hợp lệ của config
     */
    public static boolean validateConfig() {
        boolean isValid = true;

        if (VNP_TMN_CODE == null || VNP_TMN_CODE.trim().isEmpty()) {
            System.err.println("VNP_TMN_CODE is null or empty");
            isValid = false;
        }

        if (VNP_HASH_SECRET == null || VNP_HASH_SECRET.trim().isEmpty()) {
            System.err.println("VNP_HASH_SECRET is null or empty");
            isValid = false;
        }

        if (VNP_URL == null || VNP_URL.trim().isEmpty()) {
            System.err.println("VNP_URL is null or empty");
            isValid = false;
        }

        if (VNP_RETURN_URL == null || VNP_RETURN_URL.trim().isEmpty()) {
            System.err.println("VNP_RETURN_URL is null or empty");
            isValid = false;
        }

        System.out.println("VNPay config validation: " + (isValid ? "PASSED" : "FAILED"));
        return isValid;
    }
}



