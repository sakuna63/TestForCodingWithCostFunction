Attribute VB_Name = "Module1"
Option Explicit
Sub draw_graph_error_and_psnr()
    Dim i As Integer
    Dim x_offset, x_end, y_offset, y_end, y_temp As Integer
    Dim gpos_x, gpos_y, g_width, g_height As Integer
    Dim x_title, y_title As String
    Dim chart_title, graph_name As String
    Dim chart_obj As ChartObject
    Dim chart As chart
    
    ' �O���t�̃f�[�^�̊J�n�ʒu�ƏI���
    x_offset = 2
    x_end = x_offset + 11
    y_offset = 2
    
    ' �O���t�̃T�C�Y
    g_width = 1000
    g_height = 600
    
    ' �O���t�̕`��ʒu
    gpos_x = 100
    gpos_y = 20
    
    ' �e���̃^�C�g��
    x_title = "���ߍ��ݗ�"
    y_title = "SSIM"
    
    chart_title = "SSIM range=1"
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
    
     ' �f�[�^�͈͂�����
    y_temp = y_offset
    Do While Len(Cells(y_temp, 1).Value) > 0
        y_temp = y_temp + 1
    Loop
    y_end = y_temp - 1
    
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
        .Legend.Font.Size = 16                              ' �n��̃t�H���g�T�C�Y
        
        ' �f�[�^�͈͂��Z�b�g(����ƉE��)
        .SetSourceData ActiveSheet.Range( _
            Cells(y_offset, x_offset), _
            Cells(y_end, x_end) _
        ), xlColumns
        
        ' x���̍��ڎ��͈͂��Z�b�g
        For i = 1 To .SeriesCollection.Count
            .SeriesCollection(i).XValues = Range( _
                Cells(y_offset, x_offset - 1), _
                Cells(y_end, x_offset - 1) _
            )
            .SeriesCollection(i).Name = Cells(1, i + 1)
        Next
        
        With .Axes(xlCategory, xlPrimary)
            .HasTitle = True
            .HasMinorGridlines = True
            .MaximumScale = 100                           ' x���̍ő�l
            .TickLabels.Font.Size = 16
            .AxisTitle.Characters.Text = x_title
            .AxisTitle.Characters.Font.Size = 18
        End With
        With .Axes(xlValue, xlPrimary)
            .HasTitle = True
            .MaximumScale = 1
            .TickLabels.Font.Size = 16                     ' ���̐��l�̃t�H���g�T�C�Y
            .AxisTitle.Orientation = 0                      ' ���^�C�g���̊p�x�iDefault: 90)
            .AxisTitle.Top = 0
            .AxisTitle.Left = 50
            .AxisTitle.Characters.Text = y_title
            .AxisTitle.Characters.Font.Size = 18
        End With
    End With
End Sub
