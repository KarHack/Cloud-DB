/*
 * 
 * 36TH ELEMENT LICENSE 1.0
 *
 * This is a project of 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * This project is a closed source and proprietary software package.
 * None of the contents of this software is to be used for uses not intended,
 * And no one is to interface with the software in methods not defined or previously decided by 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * No changes should be done to this project without prior authorization by 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * 2018 (C) 36TH ELEMENT TECHNOLOGIES PVT. LTD.
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
public class ExtractDeviceKey extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
                        throws ServletException, IOException {
        try {
            // Lets test whatever code we need to.
            response.setContentType("text/html;charset=UTF-8");
            if ("AabXb2dq97mSbPWbzFTfk5nR7Tp2exJYbcwf86tyS992rJ5skwrUm2e6Ah7kC2RR28SKQANzaqdc25wpj8hWEZ9N6vErYLa5kVdULtJcc5tSRvvjfM9XtSJYTFefk9YyNxVats4cxVwX386p6BW2x3WMy2b7XCBuSzjCFBqHBbJ55mwGK3D5vfccqtzAZvuPRHRSGReR4QtUNhP5TH9DvUJbvPbxyHUzs6t5xw96Vwswqc9Hg4MgxwLzrBn2XP3n".equals(request.getParameter("access_token"))) {
                PrintWriter out = response.getWriter();

                String smallDeviceToken = Auth.removeUserPaddingToken(request.getParameter("device_token"));

                out.println(
                                    "{"
                                    + "\"success\":\""
                                    + smallDeviceToken
                                    + "\""
                                    + "}");

            } else {
                PrintWriter out = response.getWriter();
                out.println(
                                    "{"
                                    + "\"error\":\"Failed Authentication\""
                                    + "}");
            }
        } catch (Exception e) {
            // There was an Error.
        }
    }
}
