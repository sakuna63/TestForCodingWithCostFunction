Attribute VB_Name = "Module1"
Option Explicit
Sub draw_graph_error_and_psnr()
    Dim area, i As Integer
    Dim xl_pos, yl1_pos, yl2_pos As Integer
    Dim y_start, y_end, y_temp As Integer
    Dim gpos_x, gpos_y, g_width, g_height As Integer
    Dim chart_title, graph_name As String
    Dim chart_obj As ChartObject
    Dim chart As chart
    
    ' グラフのデータの開始位置と終わり
    y_start = 1
    y_end = y_start
    
    ' グラフのサイズ
    g_width = 1000
    g_height = 600
    
    ' グラフの描画位置
    gpos_x = 600
    gpos_y = 20
    
    ' 各系列の座標
    xl_pos = 2   ' 埋め込み率
    yl1_pos = 5 ' 誤り率
    yl2_pos = 3 ' PSNR
    
    ' 埋め込み範囲ごとにグラフを生成する
    For area = 1 To 8
        ' グラフのタイトルと名前の設定
        chart_title = "ErrorAndPSNR A=" & area
        graph_name = chart_title
        
        ' グラフデータの開始位置をずらす
        y_start = y_end + 1
        
        ' 同じグラフがあったら削除する
        If ActiveSheet.ChartObjects.Count > 0 Then
            For i = 1 To ActiveSheet.ChartObjects.Count
                ' グラフ名が一致するか
                If ActiveSheet.ChartObjects(i).Name = graph_name Then
                    ActiveSheet.ChartObjects(i).Delete
                    Exit For
                End If
            Next i
        End If
        
         ' データ範囲を決定
        y_temp = y_start
        Do While Cells(y_temp, 1).Value = area
            y_temp = y_temp + 1
        Loop
        y_end = y_temp - 1
        
        ' グラフの挿入
        Set chart_obj = ActiveSheet.ChartObjects.Add( _
            gpos_x, gpos_y, g_width, g_height _
        )
        chart_obj.Name = graph_name
        Set chart = chart_obj.chart
        
        ' グラフの設定
        With chart
            .ChartType = xlXYScatterLines                   ' 散布図
            .HasTitle = True
            .ChartTitle.Characters.Text = chart_title
            .SeriesCollection.NewSeries                       ' 系列の生成（左縦軸
            .SeriesCollection.NewSeries                       ' 系列の生成（右縦軸
            .Legend.Font.Size = 16                              ' 系列のフォントサイズ
            With .Axes(xlCategory, xlPrimary)
                .HasTitle = True
                .MaximumScale = 100                           ' x軸の最大値
                .TickLabels.Font.Size = 16
                .AxisTitle.Characters.Text = Cells(1, xl_pos)
                .AxisTitle.Characters.Font.Size = 18
            End With
            With .Axes(xlValue, xlPrimary)
                .HasTitle = True
                .TickLabels.Font.Size = 16                     ' 軸の数値のフォントサイズ
                .AxisTitle.Orientation = 0                      ' 軸タイトルの角度（Default: 90)
                .AxisTitle.Top = 0
                .AxisTitle.Left = 50
                .AxisTitle.Characters.Text = Cells(1, yl1_pos)
                .AxisTitle.Characters.Font.Size = 18
            End With
        End With
        
        ' 左縦軸の設定
        With chart.SeriesCollection(1)
            ' x軸の値の設定
            .XValues = Range( _
                Cells(y_start, xl_pos), _
                Cells(y_end, xl_pos) _
            )
            ' 系列の値の設定
            .Values = Range( _
                Cells(y_start, yl1_pos), _
                Cells(y_end, yl1_pos) _
            )
            .Name = Cells(1, yl1_pos)
            .MarkerStyle = xlMarkerStyleSquare
            .MarkerSize = 7
        End With
        
        ' 右縦軸の設定
        With chart.SeriesCollection(2)
            ' x軸の値の設定
            .XValues = Range( _
                Cells(y_start, xl_pos), _
                Cells(y_end, xl_pos) _
            )
            ' y軸の値の設定
            .Values = Range( _
                Cells(y_start, yl2_pos), _
                Cells(y_end, yl2_pos) _
            )
            .Name = Cells(1, yl2_pos)
            .MarkerStyle = xlMarkerStyleCircle
            .MarkerSize = 7
            ' 右軸にする
            .AxisGroup = xlSecondary
        End With
        
        ' 右y軸の設定（ここで設定しないとなぜかエラー）
        With chart.Axes(xlValue, xlSecondary)
            .HasTitle = True
            .MaximumScale = 80
            .TickLabels.Font.Size = 16
            .AxisTitle.Orientation = 0
            .AxisTitle.Top = 0
            .AxisTitle.Left = 800
            .AxisTitle.Characters.Text = Cells(1, yl2_pos)
            .AxisTitle.Characters.Font.Size = 18
        End With
                
        ' グラフの描画位置をずらす
        gpos_y = gpos_y + g_height + 50
        
    Next
End Sub
