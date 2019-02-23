/*
 * 
 * 36E CLOUD CONNECTOR
 * This is a project of 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * This project is a closed source and proprietary software package.
 * None of the contents of this software is to be used for uses not intended,
 * And no one is to interface with the software in methods not defined or previously decided by 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * No changes should be done to this project without prior authorization by 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * 2017 (C) 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * 
 */
package security;

import helpers.Helper;

/**
 *
 * @author karan. Will be used to help authenticate in the system.
 *
 */
public class Auth {

    public static String create() {
	try {
	    // Here we create a new authentication key.
	    return Security.getSaltString(128);

	} catch (Exception e) {
	    // There was an error.
	    return e.getLocalizedMessage();
	}
    }

    // Validate a Authenticate Token.
    public static boolean validate(String token) {
	try {
	    // Here we will validate the Token.
	    return token.length() == 256;
	} catch (Exception e) {
	    // There was an Error.
	    return false;
	}
    }

    // Lets Generate the Auth Token that will be used for Authenticating at the API Level.
    public static String generateToken(String userToken, String deviceSToken) {
	try {
	    // Lets Generate the String Now.
	    if (userToken.length() == 160 && deviceSToken.length() == 96) {
		// Both the Tokens are of proper size.
		// Lets Divide the Device Token into Even and Odd.
		String[] deviceTokenEvenOdd = evenOddSplit(deviceSToken);
		// Insert Even Numbers into the Token using Fibonacci.
		String[] tokenFibo = insertFibo(userToken, deviceTokenEvenOdd[0]);
		userToken = tokenFibo[0];
		deviceTokenEvenOdd[0] = tokenFibo[1];
		// Insert Odd Numbers into the Token using Fibonacci.
		tokenFibo = insertFibo(userToken, deviceTokenEvenOdd[1]);
		userToken = tokenFibo[0];
		deviceTokenEvenOdd[1] = tokenFibo[1];
		// Now we have to Insert Even Numbers into the Token using Reverse Fibonacci.
		tokenFibo = insertFibo(new StringBuilder(userToken).reverse().toString(),
			deviceTokenEvenOdd[0]);
		userToken = tokenFibo[0];
		deviceTokenEvenOdd[0] = tokenFibo[1];
		// Now we have to Insert Odd Numbers into the Token using Reverse Fibonacci.
		tokenFibo = insertFibo(userToken, deviceTokenEvenOdd[1]);
		userToken = tokenFibo[0];
		deviceTokenEvenOdd[1] = tokenFibo[1];
		// Now we have to Insert Even Numbers into the Token using Fibonacci.
		tokenFibo = insertFibo(new StringBuilder(userToken).reverse().toString(),
			deviceTokenEvenOdd[0]);
		userToken = tokenFibo[0];
		deviceTokenEvenOdd[0] = tokenFibo[1];
		// Now we have to Insert Odd Numbers into the Token using Fibonacci.
		tokenFibo = insertFibo(userToken, deviceTokenEvenOdd[1]);
		userToken = tokenFibo[0];
		deviceTokenEvenOdd[1] = tokenFibo[1];

		// Extract the Last 3 Characters from both Even & Odd Device Strings.
		userToken = userToken + deviceTokenEvenOdd[0].substring(0, 3)
			+ deviceTokenEvenOdd[1].substring(0, 3);
		deviceTokenEvenOdd[0] = deviceTokenEvenOdd[0].substring(3, deviceTokenEvenOdd[0].length());
		deviceTokenEvenOdd[1] = deviceTokenEvenOdd[1].substring(3, deviceTokenEvenOdd[1].length());

		// Now we have to Insert Even Numbers into the Token using Fibonacci.
		tokenFibo = insertFibo(new StringBuilder(userToken).reverse().toString(),
			deviceTokenEvenOdd[0]);
		userToken = tokenFibo[0];
		deviceTokenEvenOdd[0] = tokenFibo[1];
		// Now we have to Insert Odd Numbers into the Token using Fibonacci.
		tokenFibo = insertFibo(userToken, deviceTokenEvenOdd[1]);
		userToken = tokenFibo[0];
		deviceTokenEvenOdd[1] = tokenFibo[1];

		return new StringBuilder(userToken).reverse().toString();
	    } else {
		// Both the Tokens are not of Proper Size.
		// And the Token Cannot be Generated.
		return "Err : Tokens Not of Proper Size";
	    }
	} catch (Exception e) {
	    // There was an Error.
	    return null;
	}
    }

    // This method will help us split the Provided String into Even & Odd Characters String
    private static String[] evenOddSplit(String textToDivide) {
	try {
	    // Here we will try to Split the String into the Even and Odd.
	    StringBuilder evenStr = new StringBuilder();
	    StringBuilder oddStr = new StringBuilder();
	    // Lets Extract the Even and Odd.
	    if (textToDivide.length() % 2 == 1) {
		textToDivide = textToDivide + '%';
	    }
	    int i = 0;
	    while (i < textToDivide.length()) {
		// Lets Divide the Text.
		evenStr.append(textToDivide.charAt(i));
		oddStr.append(textToDivide.charAt(i + 1));
		i = i + 2;
	    }
	    if (oddStr.toString().endsWith("%")) {
		String replacedStr = oddStr.toString().replaceAll("%", "");
		oddStr = new StringBuilder(replacedStr);
	    }
	    return new String[]{evenStr.toString(), oddStr.toString()};
	} catch (Exception e) {
	    // There was an Error.
	    return null;
	}
    }

    // Insert using FIBONACCI of the Given String.
    private static String[] insertFibo(String token, String fiboStr) {
	try {
	    // Here we will insert the fibo string in sequence into the tokne using fibonacci series into the token.
	    int iterates = 0;
	    int prevFib = 1;
	    int fiboIndex = 1;
	    while (token.length() > fiboIndex) {
		String tempStr = token.substring(0, fiboIndex);;
		tempStr = tempStr + fiboStr.charAt(iterates);
		tempStr = tempStr + token.substring(fiboIndex);
		token = tempStr;
		int tempInt = prevFib;
		prevFib = fiboIndex;
		fiboIndex = fiboIndex + tempInt;
		iterates = iterates + 1;
	    }
	    return new String[]{token, fiboStr.substring(iterates)};
	} catch (Exception e) {
	    // There was an Error.
	    return null;
	}
    }

    // Extract the User Token and the Device / Server Token.
    public static String[] extractToken(String token) {
	try {
	    // Here we will extract the required data from the token.
	    if (token.length() == 256) {
		// It is probably a valid token.
		String[] tokenData = new String[2];
		String dTokenEvenBuild = "";
		String dTokenOddBuild = "";
		StringBuilder tokenBuild = new StringBuilder(token);
		// Lets Extract the Device Token and the User Token Using Reverse FIBONACCI.
		tokenData = extractFibo(tokenBuild.reverse().toString());
		tokenBuild = new StringBuilder(tokenData[0]);
		dTokenOddBuild = new StringBuilder(tokenData[1]).toString();
		// Lets Extract the next Device Token and the User Token using Reverse FIBO.
		tokenData = extractFibo(tokenBuild.toString());
		tokenBuild = new StringBuilder(tokenData[0]);
		dTokenEvenBuild = new StringBuilder(tokenData[1]).toString();

		// Extract the First 6 Characters of the Token and Add Respectivly to EVEN and ODD.
		String tempStr = tokenBuild.reverse().toString().substring(tokenBuild.length() - 6);
		tokenBuild = new StringBuilder(tokenBuild.toString().substring(0, tokenBuild.length() - 6));
		dTokenEvenBuild = tempStr.substring(0, 3) + dTokenEvenBuild;
		dTokenOddBuild = tempStr.substring(3) + dTokenOddBuild;

		// Lets Extract the Device Token and the User Token Using FIBONACCI.
		tokenData = extractFibo(tokenBuild.toString());
		tokenBuild = new StringBuilder(tokenData[0]);
		dTokenOddBuild = new StringBuilder(tokenData[1]).toString() + dTokenOddBuild;
		// Lets Extract the next Device Token and the User Token using FIBO.
		tokenData = extractFibo(tokenBuild.toString());
		tokenBuild = new StringBuilder(tokenData[0]);
		dTokenEvenBuild = new StringBuilder(tokenData[1]).toString() + dTokenEvenBuild;

		// Lets Extract the Device Token and the User Token Using Reverse FIBONACCI.
		tokenData = extractFibo(tokenBuild.reverse().toString());
		tokenBuild = new StringBuilder(tokenData[0]);
		dTokenOddBuild = new StringBuilder(tokenData[1]).toString() + dTokenOddBuild;
		// Lets Extract the next Device Token and the User Token using Reverse FIBO.
		tokenData = extractFibo(tokenBuild.toString());
		tokenBuild = new StringBuilder(tokenData[0]);
		dTokenEvenBuild = new StringBuilder(tokenData[1]).toString() + dTokenEvenBuild;

		// Lets Extract the Device Token and the User Token Using FIBONACCI.
		tokenData = extractFibo(tokenBuild.reverse().toString());
		tokenBuild = new StringBuilder(tokenData[0]);
		dTokenOddBuild = new StringBuilder(tokenData[1]).toString() + dTokenOddBuild;
		// Lets Extract the next Device Token and the User Token using FIBO.
		tokenData = extractFibo(tokenBuild.toString());
		tokenBuild = new StringBuilder(tokenData[0]);
		dTokenEvenBuild = new StringBuilder(tokenData[1]).toString() + dTokenEvenBuild;

		return new String[]{tokenBuild.toString(),
		    mergeEvenOdd(dTokenEvenBuild, dTokenOddBuild)};
	    } else {
		// It is not a Valid Token.
		return new String[]{null, null};
	    }
	} catch (Exception e) {
	    // There was an Error.
	    return new String[]{null, null};
	}
    }

    // Reverse Extraction FIBBONACCI of the Given String.
    static String[] extractFibo(String token) {
	try {
	    // Here we will extract the characters from the token that are in the FIBO Sequence.
	    StringBuilder deviceToken = new StringBuilder();
	    // Lets Run the FIBO Extraction.
	    int prevFibo = 1;
	    int fiboIndex = 1;
	    // Start the spliting process.
	    while (token.length() > fiboIndex) {
		deviceToken.append(token.charAt(fiboIndex));
		int tempFibo = fiboIndex;
		// Replace the Character at fibo index.
		String tempStr = token;
		token = token.substring(0, fiboIndex);
		token = token + '^' + tempStr.substring(fiboIndex + 1);
		fiboIndex = fiboIndex + prevFibo;
		prevFibo = tempFibo;
	    }

	    return new String[]{token.replace("^", ""), deviceToken.toString()};
	} catch (Exception e) {
	    // There was an Error.
	    return new String[]{token, Helper.Error.getErrorMessage(e)};
	}
    }

    // Merget the Even and Odd Strings into one single string.
    private static String mergeEvenOdd(String evenString, String oddString) {
	try {
	    // Here we merge the Even and The Odd Strings.
	    if (evenString.length() == oddString.length()) {
		StringBuilder mergedStrBuild = new StringBuilder();
		int iterator = 0;
		while (iterator < evenString.length()) {
		    mergedStrBuild.append(evenString.charAt(iterator))
			    .append(oddString.charAt(iterator));
		    iterator = iterator + 1;
		}

		return mergedStrBuild.toString();
	    } else {
		// The Strings cannot be merged.
		return "String not of Equal Length : " + evenString.length() + " :: "
			+ oddString.length() + "  ::::    " + evenString + " :: " + oddString;
	    }
	} catch (Exception e) {
	    // There was an Error.
	    return null;
	}
    }

    public static String addUserPaddingToDeviceToken(String userToken, String deviceToken) {
	try {
	    // Here we will Add the Required Padding to the Device Token.
	    if (userToken.length() == 160 && deviceToken.length() == 96) {
		// Extract the Required Characters using Fibonacci.
		// Extract the First 11 Characters.
		String[] tokens = Auth.extractFibo(userToken);
		StringBuilder deviceTokenPad = new StringBuilder(tokens[1]);
		// Extract the Second 11 Characters using Reverse FIBO.
		tokens = Auth.extractFibo(tokens[0]);
		deviceTokenPad.append(new StringBuilder(tokens[1]).reverse().toString());
		// Extract the Last 10 Characters Using Normal FIBO.
		tokens = Auth.extractFibo(tokens[0]);
		deviceTokenPad.append(tokens[1]);

		// Merge the Extracted Characters into the Device Token.
		char[] deviceTokenPadChar = deviceTokenPad.toString().toCharArray();
		for (int i = 0; i < deviceTokenPadChar.length; i++) {
		    // Lets Add the Characters here.
		    deviceToken = deviceToken.substring(0, i * 4)
			    + deviceTokenPadChar[i]
			    + deviceToken.substring(i * 4);
		}
		// Return the Generated String.
		return deviceToken;
	    } else {
		// The Strings were not of Correct Lenght.
		return "ERR : Strings not of Correct Length :: U : "
			+ userToken.length() + " :: D : " + deviceToken.length();
	    }
	} catch (Exception e) {
	    // There was an Error.
	    return "ERR : " + Helper.Error.getErrorMessage(e);
	}
    }
    
    public static String removeUserPaddingToken(String deviceToken) {
	try {
	    // Here we will remove the User Padding from the device token.
	    if (deviceToken.length() == 128) {
		// The Device token is of proper length. 
		for (int i = 0; i < 32; i++) {
		    // Lets now replace the character at the index we want to remove the Character.
		    deviceToken = deviceToken.substring(0, i * 4)
				    + " " + deviceToken.substring((i * 4) + 1);
		}
		String temp = deviceToken.replaceAll(" ", "");
		// Replace all the delimiters to no Character.
		return temp;
	    } else {
		// The Device token is not of proper length.
		return "Device token of Improper Length";
	    }
	} catch (Exception e) {
	    // There was an Error.
	    return null;
	}
    }
    
}
