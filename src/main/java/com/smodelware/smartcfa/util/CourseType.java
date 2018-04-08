package com.smodelware.smartcfa.util;

public enum CourseType {

    CFA_LEVEL_1("2018_CFA_LEVEL_1"),
    CFA_LEVEL_3("2018_CFA_LEVEL_3");

    private String courseType;

    private CourseType(String courseType)
    {
        this.courseType = courseType;
    }

    public String getCourseType() {
        return courseType;
    }


    public static CourseType getContentTypeFromValue(String inputValue)
    {
        for(CourseType ct:values())
        {
            if(ct.getCourseType().equalsIgnoreCase(inputValue))
            {
                return ct;
            }
        }
        return null;
    }
}
