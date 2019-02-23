/*
 * 
 * 36TH ELEMENT LICENSE 1.0
 *
 * This is a project of 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * This project is a closed source and proprietary software package.
 * None of the contents of this software is to be used for uses not intended,
 * And no one is to interface with the software in methods not defined or previously decided by 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * No changes should be done to this project without prior authorization by 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * 2017 (C) 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * 
 * 
 */
package cloudDBEndpoints;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import security.Auth;

/**
 *
 * @author karan
 */
public class Tester extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
                        throws ServletException, IOException {
        try {
            // Lets test whatever code we need to.
            response.setContentType("text/html;charset=UTF-8");
            if (request.getRequestURL().toString().substring(7).startsWith("localhost")) {
                PrintWriter out = response.getWriter();
                
                out.println("User Token : " + request.getParameter("user_token"));
                out.println();
                out.println("Other Token : " + request.getParameter("other_token"));
                out.println();
                out.println();
                out.println("----------------------------------------------------------------------------------------------------");
                out.println();
                out.println();
                
                out.println(Auth.generateToken(request.getParameter("user_token"),
                                    request.getParameter("other_token")));
            } else {
                PrintWriter out = response.getWriter();
                out.println("Can't Show Here");
            }

            /*String genToken = Auth.generateToken(request.getParameter("user_token"),
		    request.getParameter("other_token"));
	    out.println("Token : " + genToken);
	    
	    out.println();
	    out.println();
	    out.println();
	    out.println("----------------------------------------------------------------------------------------------------");
	    out.println("----------------------------------------------------------------------------------------------------");

	    out.println();
	    out.println();

	    out.println(Auth.extractToken(genToken)[0]);
	    out.println();
	    out.println();
	    out.println("----------------------------------------------------------------------------------------------------");
	    out.println();
	    out.println(Auth.extractToken(genToken)[1]);
	    out.println("****************************************************************************************************");
	    out.println();
	    out.println(Auth.addUserPaddingToDeviceToken("3C305GCdxLSn1lsmIwb2AbzRTjrcT7skj6nmql7HzwmUTFyxK1meKdSVeY1fbpMh6UhXvaflBcKOrZM4gu2DQCJlzNbkSf3RF0tfJatNwckSqsGETAtdPYZCXhvA1q5uHSFAfaxo4KM1wIa190DBgp1f76l0dk5N",
		    "NBu65HLBYprq42wzPjVg9ulSJLa9ybLL9m9Vjl4tdR5PEjfK76cPTyGv5qNIHewnCWBSOIv2u6NV9C3UvUbQDjsXM5ubG0Dg"));
             */
        } catch (Exception e) {
            // There was an Error.
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
                        throws ServletException, IOException {
    }
    
}
