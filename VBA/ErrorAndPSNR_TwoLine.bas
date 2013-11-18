Attribute VB_Name = "Module11"
Option Explicit
Sub draw_graph_error_and_psnr()
    Dim i, j As Integer
    Dim xl_pos, yl1_pos, yl2_pos As Integer
    Dim y_start, y_end, y_temp As Integer
    Dim gpos_x, gpos_y, g_width, g_height As Integer
    Dim chart_title, graph_name As String
    Dim chart_obj As ChartObject
    Dim chart As chart
    
    ' �O���t�̃f�[�^�̊J�n�ʒu�ƏI���
    y_start = 2
    y_end = y_start
    
    ' �O���t�̃T�C�Y
    g_width = 1000
    g_height = 600
    
    ' �O���t�̕`��ʒu
    gpos_x = 600
    gpos_y = 20
    
    ' �e�n��̍��W
    xl_pos = 1   ' �t�@�C����
    yl1_pos = 5 ' ��藦
    yl2_pos = 3 ' PSNR
    
    ' ���ߍ��ݔ͈͂��ƂɃO���t�𐶐�����
    For j = 8 To 256
        ' �O���t�̃^�C�g���Ɩ��O�̐ݒ�
        chart_title = "ErrorAndPSNR rate=" & Cells(y_start, 2)
        graph_name = chart_title
        
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
        
        y_end = y_start + 11
        
        ' �O���t�̑}��
        Set chart_obj = ActiveSheet.ChartObjects.Add( _
            gpos_x, gpos_y, g_width, g_height _
        )
        chart_obj.Name = graph_name
        Set chart = chart_obj.chart
        
        ' �O���t�̐ݒ�
        With chart
            .HasTitle = True
            .ChartTitle.Characters.Text = chart_title
            .SeriesCollection.NewSeries                       ' �n��̐����i���c��
            .SeriesCollection.NewSeries                       ' �n��̐����i�_�~�[
            .SeriesCollection.NewSeries                       ' �n��̐����i�_�~�[
            .SeriesCollection.NewSeries                       ' �n��̐����i�E�c��
            .Legend.Font.Size = 16                              ' �n��̃t�H���g�T�C�Y
            With .Axes(xlCategory, xlPrimary)
                .HasTitle = True
                .TickLabels.Font.Size = 16
                .AxisTitle.Characters.Text = Cells(1, xl_pos)
                .AxisTitle.Characters.Font.Size = 18
            End With
            With .Axes(xlValue, xlPrimary)
                .HasTitle = True
                .MaximumScale = 60
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
                Cells(y_start, xl_pos), _
                Cells(y_end, xl_pos) _
            )
            ' �n��̒l�̐ݒ�
            .Values = Range( _
                Cells(y_start, yl1_pos), _
                Cells(y_end, yl1_pos) _
            )
            .Name = Cells(1, yl1_pos)
            .MarkerStyle = xlMarkerStyleSquare
            .MarkerSize = 7
            .ChartType = xlColumnClustered
        End With
        
        ' �_�~�[���̐ݒ�
        With chart.SeriesCollection(2)
            ' x���̒l�̐ݒ�
            .XValues = Range( _
                Cells(y_start, 6), _
                Cells(y_end, 6) _
            )
            ' y���̒l�̐ݒ�
            .Values = Range( _
                Cells(y_start, 6), _
                Cells(y_end, 6) _
            )
            .Name = Cells(1, yl2_pos)
            .MarkerStyle = xlMarkerStyleCircle
            .MarkerSize = 7
            .ChartType = xlColumnClustered
        End With
        
        ' �_�~�[�c���̐ݒ�
        With chart.SeriesCollection(3)
            ' x���̒l�̐ݒ�
            .XValues = Range( _
                Cells(y_start, 6), _
                Cells(y_end, 6) _
            )
            ' y���̒l�̐ݒ�
            .Values = Range( _
                Cells(y_start, 6), _
                Cells(y_end, 6) _
            )
            .Name = Cells(1, yl2_pos)
            .MarkerStyle = xlMarkerStyleCircle
            .MarkerSize = 7
            .AxisGroup = xlSecondary
            .ChartType = xlColumnClustered
        End With
        
        ' �E�c���̐ݒ�
        With chart.SeriesCollection(4)
            ' x���̒l�̐ݒ�
            .XValues = Range( _
                Cells(y_start, xl_pos), _
                Cells(y_end, xl_pos) _
            )
            ' y���̒l�̐ݒ�
            .Values = Range( _
                Cells(y_start, yl2_pos), _
                Cells(y_end, yl2_pos) _
            )
            .Name = Cells(1, yl2_pos)
            .Format.Fill.ForeColor.ObjectThemeColor = msoThemeColorAccent2
            ' �E���ɂ���
            .AxisGroup = xlSecondary
            .ChartType = xlColumnClustered
        End With
        
        '�_�~�[��legend������
        chart.Legend.LegendEntries(2).Delete
        chart.Legend.LegendEntries(2).Delete
        
        ' �Ey���̐ݒ�i�����Őݒ肵�Ȃ��ƂȂ����G���[�j
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
                
        ' �O���t�f�[�^�̊J�n�ʒu�����炷
        y_start = y_end + 1
        
        ' �O���t�̕`��ʒu�����炷
        gpos_y = gpos_y + g_height + 50
        
    Next
End Sub
