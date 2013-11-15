Attribute VB_Name = "Module4"
Option Explicit
Sub draw_graph_error_and_psnr_f_to_l()
    Dim area, i As Integer
    Dim xl_pos, yl1_pos, yl2_pos As Integer
    Dim y_start, y_end, y_first, y_last, xl_max, xl_min, y_temp As Integer
    Dim gpos_x, gpos_y, g_width, g_height As Integer
    Dim chart_title, graph_name As String
    Dim chart_obj As ChartObject
    Dim chart As chart
    
    ' �O���t�̃f�[�^�̊J�n�ʒu�ƏI���
    y_start = 1
    y_end = y_start
    
    ' x�̒��ł��g�p����f�[�^�͈̔�
    xl_min = 30
    xl_max = 50
    
    ' �O���t�̃T�C�Y
    g_width = 1000
    g_height = 600
    
    ' �O���t�̕`��ʒu
    gpos_x = 1650
    gpos_y = 20
    
    ' �e�n��̍��W
    xl_pos = 2   ' ���ߍ��ݗ�
    yl1_pos = 5 ' ��藦
    yl2_pos = 3 ' PSNR
    
    ' ���ߍ��ݔ͈͂��ƂɃO���t�𐶐�����
    For area = 1 To 8
        ' �O���t�̃^�C�g���Ɩ��O�̐ݒ�
        chart_title = "ErrorAndPSNR" & xl_min & "to" & xl_max & " A=" & area
        graph_name = chart_title
        
        ' �O���t�f�[�^�̊J�n�ʒu�����炷
        y_start = y_end + 1
        
        ' �����O���t����������폜����
        If ActiveSheet.ChartObjects.Count > 0 Then
            For i = 1 To ActiveSheet.ChartObjects.Count
                ' �O���t������v���邩
                If ActiveSheet.ChartObjects(i).Name = graph_name Then
                    ActiveSheet.ChartObjects(i).Delete
                    Exit For
                End If
            Next i
        End If
        
         ' �f�[�^�͈͂�����
        y_temp = y_start
        Do While Cells(y_temp, 1).Value = area
            y_temp = y_temp + 1
        Loop
        y_end = y_temp - 1
        
        For y_temp = y_start To y_end
            If Cells(y_temp, xl_pos) <= xl_max Then
                y_first = y_temp
                Exit For
            End If
        Next
        
        For y_temp = y_start To y_end
            If Cells(y_temp, xl_pos) <= xl_min Then
                y_last = y_temp - 1
                Exit For
            End If
        Next
        
        ' �O���t�̑}��
        Set chart_obj = ActiveSheet.ChartObjects.Add( _
            gpos_x, gpos_y, g_width, g_height _
        )
        chart_obj.Name = graph_name
        Set chart = chart_obj.chart
        
        ' �O���t�̐ݒ�
        With chart
            .ChartType = xlXYScatterLines                   ' �U�z�}
            .HasTitle = True
            .ChartTitle.Characters.Text = chart_title
            .SeriesCollection.NewSeries                       ' �n��̐����i���c��
            .SeriesCollection.NewSeries                       ' �n��̐����i�E�c��
            .Legend.Font.Size = 16                              ' �n��̃t�H���g�T�C�Y
            With .Axes(xlCategory, xlPrimary)
                .HasTitle = True
                .MinimumScale = xl_min
                .MaximumScale = xl_max
                .TickLabels.Font.Size = 16
                .AxisTitle.Characters.Text = Cells(1, xl_pos)
                .AxisTitle.Characters.Font.Size = 18
            End With
            With .Axes(xlValue, xlPrimary)
                .HasTitle = True
                .TickLabels.Font.Size = 16                     ' ���̐��l�̃t�H���g�T�C�Y
                .AxisTitle.Orientation = 0                      ' ���^�C�g���̊p�x�iDefault: 90)
                .AxisTitle.Top = 0
                .AxisTitle.Left = 50
                .AxisTitle.Characters.Text = Cells(1, yl1_pos)
                .AxisTitle.Characters.Font.Size = 18
            End With
        End With
        
        ' ���c���̐ݒ�
        With chart.SeriesCollection(1)
            ' x���̒l�̐ݒ�
            .XValues = Range( _
                Cells(y_first, xl_pos), _
                Cells(y_last, xl_pos) _
            )
            ' �n��̒l�̐ݒ�
            .Values = Range( _
                Cells(y_first, yl1_pos), _
                Cells(y_last, yl1_pos) _
            )
            .Name = Cells(1, yl1_pos)
            .MarkerStyle = xlMarkerStyleSquare
            .MarkerSize = 7
        End With
        
        ' �E�c���̐ݒ�
        With chart.SeriesCollection(2)
            ' x���̒l�̐ݒ�
            .XValues = Range( _
                Cells(y_first, xl_pos), _
                Cells(y_last, xl_pos) _
            )
            ' y���̒l�̐ݒ�
            .Values = Range( _
                Cells(y_first, yl2_pos), _
                Cells(y_last, yl2_pos) _
            )
            .Name = Cells(1, yl2_pos)
            .MarkerStyle = xlMarkerStyleCircle
            .MarkerSize = 7
            ' �E���ɂ���
            .AxisGroup = xlSecondary
        End With
        
        ' �Ey���̐ݒ�i�����Őݒ肵�Ȃ��ƂȂ����G���[�j
        With chart.Axes(xlValue, xlSecondary)
            .HasTitle = True
            .TickLabels.Font.Size = 16
            .AxisTitle.Orientation = 0
            .AxisTitle.Top = 0
            .AxisTitle.Left = 800
            .AxisTitle.Characters.Text = Cells(1, yl2_pos)
            .AxisTitle.Characters.Font.Size = 18
        End With
                
        ' �O���t�̕`��ʒu�����炷
        gpos_y = gpos_y + g_height + 50
        
    Next
End Sub


