Attribute VB_Name = "Module1"
Sub test()
    Dim i As Long
 
    Application.ScreenUpdating = False
    On Error Resume Next
    
    For i = 1 To 21
        ActiveChart.SeriesCollection(i).Select
        Selection.Border.Color = RGB(0, 0, 0)
        Selection.MarkerBackgroundColor = RGB(0, 0, 0)
        Selection.MarkerForegroundColor = RGB(0, 0, 0)
    Next i
    Application.ScreenUpdating = True
End Sub
