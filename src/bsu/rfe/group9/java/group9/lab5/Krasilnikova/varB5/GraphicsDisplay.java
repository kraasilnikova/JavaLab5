package bsu.rfe.group9.java.group9.lab5.Krasilnikova.varB5;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.JPanel;
import java.awt.event.*;
import java.util.EmptyStackException;
import java.util.Stack;

public class GraphicsDisplay extends JPanel
{
    class GraphPoint
    {
        double xd;
        double yd;
        int x;
        int y;
        int n;
    }
    class Zone
    {
        double MAXY;
        double tmp;
        double MINY;
        double MAXX;
        double MINX;
        boolean use;
    }

    // Список координат точек для построения графика
    private Double[][] graphicsData;
    // Флаговые переменные, задающие правила отображения графика
    private boolean showAxis = true;
    private boolean showMarkers = true;

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    // Используемый масштаб отображения
    private double scale;
    // Различные стили черчения линий
    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;
    // Различные шрифты отображения надписей
    private Font axisFont;

    private Font captionFont;
    private boolean transform = false;
    private GraphPoint SMP;
    private DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance();
    private BasicStroke selStroke;
    private int[][] graphicsDataI;
    private boolean selMode = false;
    private boolean dragMode = false;
    private boolean zoom = false;
    private int mausePX = 0;
    private int mausePY = 0;
    private Rectangle2D.Double rect;
    private double scaleX;
    private double scaleY;
    private Zone zone = new Zone();
    private Stack<Zone> stack = new Stack<Zone>();

    public GraphicsDisplay()
    {
        setBackground(Color.WHITE);
        graphicsStroke = new BasicStroke(5.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 10.0f, new float[] {30, 10, 20, 10, 10, 10, 20, 10}, 0.0f);
        axisStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        markerStroke = new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 45.0f, null, 0.0f);
        axisFont = new Font("Serif", Font.BOLD, 15);
        selStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{8, 8}, 0.0f);
        captionFont = new Font("Serif", Font.BOLD, 10);

        MouseMotionHandler mouseMotionHandler = new MouseMotionHandler();
        addMouseMotionListener(mouseMotionHandler);
        addMouseListener(mouseMotionHandler);
        rect = new Rectangle2D.Double();
        zone.use = false;
    }

    // Данный метод вызывается из обработчика элемента меню "Открыть файл с графиком"
    public void showGraphics(Double[][] graphicsData)
    {
        // Сохранить массив точек во внутреннем поле класса
        this.graphicsData = graphicsData;
        graphicsDataI = new int[graphicsData.length][2];
        // Запросить перерисовку компонента, т.е. неявно вызвать paintComponent()
        repaint();
    }
    // Методы-модификаторы для изменения параметров отображения графика
    public void setShowAxis(boolean showAxis)
    {
        this.showAxis = showAxis;
        repaint();
    }
    public void setShowMarkers(boolean showMarkers)
    {
        this.showMarkers = showMarkers;
        repaint();
    }
    // Метод отображения всего компонента, содержащего график
    public void paintComponent(Graphics g)
    {// Шаг 1 - Вызвать метод предка для заливки области цветом заднего фона
        super.paintComponent(g);
        // Шаг 2 - Если данные графика не загружены (при показе компонента при запуске программы) - ничего не делать
        if (graphicsData==null || graphicsData.length==0) return;
        // Шаг 3 - Определить минимальное и максимальное значения для координат X и Y
        minX = graphicsData[0][0];
        maxX = graphicsData[graphicsData.length-1][0];

        if (zone.use)
        {
            minX = zone.MINX;
        }
        if (zone.use)
        {
            maxX = zone.MAXX;
        }

        minY = graphicsData[0][1];
        maxY = minY;
        for (int i = 1; i < graphicsData.length; i++)
        {
            if (graphicsData[i][1]<minY)
            {
                minY = graphicsData[i][1];
            }
            if (graphicsData[i][1]>maxY)
            {
                maxY = graphicsData[i][1];
            }
        }

        if (zone.use)
        {
            minY = zone.MINY;
        }
        if (zone.use)
        {
            maxY = zone.MAXY;
        }

        // Шаг 4 - Определить (исходя из размеров окна) масштабы по осям X и Y - сколько пикселов приходится на единицу длины по X и по Y
        scaleX = 1.0 / (maxX - minX);
        scaleY = 1.0 / (maxY - minY);

        if (!transform)
            scaleX *= getSize().getWidth();
        else
            scaleX *= getSize().getHeight();
        if (!transform)
            scaleY *= getSize().getHeight();
        else
            scaleY *= getSize().getWidth();
        if (transform)
        {
            ((Graphics2D) g).rotate(-Math.PI / 2);
            ((Graphics2D) g).translate(-getHeight(), 0);
        }

        // Шаг 5 - Чтобы изображение было неискажѐнным - масштаб должен быть одинаков
        // Выбираем за основу минимальный
        scale = Math.min(scaleX, scaleY);
        // Шаг 6 - корректировка границ отображаемой области согласно выбранному масштабу
        if (!zoom)
        {
            if (scale == scaleX)
            {
                double yIncrement = 0;
                if (!transform)
                    yIncrement = (getSize().getHeight() / scale - (maxY - minY)) / 2;
                else
                    yIncrement = (getSize().getWidth() / scale - (maxY - minY)) / 2;

                maxY += yIncrement;
                minY -= yIncrement;
            }
            if (scale == scaleY)
            {
                double xIncrement = 0;
                if (!transform)
                    xIncrement = (getSize().getWidth() / scale - (maxX - minX)) / 2;
                else
                    xIncrement = (getSize().getHeight() / scale - (maxX - minX)) / 2;
                maxX += xIncrement;
                minX -= xIncrement;
            }
        }
        // Шаг 7 - Сохранить текущие настройки холста
        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();
        // Шаг 8 - В нужном порядке вызвать методы отображения элементов графика
        // Порядок вызова методов имеет значение, т.к. предыдущий рисунок будет затираться последующим
        // Первыми (если нужно) отрисовываются оси координат.
        if (showAxis) paintAxis(canvas);
        // Затем отображается сам график
        paintGraphics(canvas);
        // Затем (если нужно) отображаются маркеры точек, по которым строился график.
        if (showMarkers) paintMarkers(canvas);
        if (SMP != null)
            paintHint(canvas);
        if (selMode)
        {
            canvas.setColor(Color.LIGHT_GRAY);
            canvas.setStroke(selStroke);
            canvas.draw(rect);
        }

        // Шаг 9 - Восстановить старые настройки холста
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
    }

    protected void paintHint(Graphics2D canvas)
    {
        Color oldColor = canvas.getColor();
        canvas.setColor(Color.DARK_GRAY);
        StringBuffer label = new StringBuffer();
        label.append("(X = ");
        label.append(formatter.format((SMP.xd)));
        label.append("; Y = ");
        label.append(formatter.format((SMP.yd)));
        label.append(")");
        FontRenderContext context = canvas.getFontRenderContext();
        Rectangle2D bounds = captionFont.getStringBounds(label.toString(), context);
        if (!transform)
        {
            int dy = -10;
            int dx = +7;
            if (SMP.y < bounds.getHeight())
                dy = +13;
            if (getWidth() < bounds.getWidth() + SMP.x + 20)
                dx = -(int) bounds.getWidth() - 15;
            canvas.drawString(label.toString(), SMP.x + dx, SMP.y + dy);
        } else
            {
            int dy = 10;
            int dx = -7;
            if (SMP.x < 10)
                dx = +13;
            if (SMP.y < bounds.getWidth() + 20)
                dy = -(int) bounds.getWidth() - 15;
            canvas.drawString(label.toString(), getHeight() - SMP.y + dy, SMP.x + dx);
        }
        canvas.setColor(oldColor);
    }

    // Отрисовка графика по прочитанным координатам
    protected void paintGraphics(Graphics2D canvas)
    {
        // Выбрать линию для рисования графика
        canvas.setStroke(graphicsStroke);
        canvas.setColor(Color.PINK);
        /* Будем рисовать линию графика как путь, состоящий из множества сегментов (GeneralPath)
         * Начало пути устанавливается в первую точку графика, после чего прямой соединяется со
         * следующими точками
         */
        GeneralPath graphics = new GeneralPath();
        for (int i = 0; i < graphicsData.length; i++)
        {
            // Преобразовать значения (x,y) в точку на экране point
            Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
            graphicsDataI[i][0] = (int) point.getX();
            graphicsDataI[i][1] = (int) point.getY();
            if (transform)
            {
                graphicsDataI[i][0] = (int) point.getY();
                graphicsDataI[i][1] = getHeight() - (int) point.getX();
            }


            if (i>0)
            {
                // Не первая итерация цикла - вести линию в точку point
                graphics.lineTo(point.getX(), point.getY());
            } else
            {
                // Первая итерация цикла - установить начало пути в точку point
                graphics.moveTo(point.getX(), point.getY());
            }
        }
        // Отобразить график
        canvas.draw(graphics);
    }
    private boolean markPoint(double y)
    {
        int n = (int) y;
        if (n < 0)
            n *= (-1);
        while (n != 0)
        {
            int q = n - (n / 10) * 10;
            if (q % 2 != 0)
                return false;
            n = n / 10;
        }
        return true;
    }

    protected void paintMarkers(Graphics2D canvas)
    {
        canvas.setStroke(markerStroke);
        canvas.setColor(Color.BLACK);
        for (int i = 0; i < graphicsData.length; i++)
        {
            Boolean flag = true;
            if (i != 0 &&  i != graphicsData.length - 1 &&((graphicsData[i-1][1] < graphicsData[i][1] && graphicsData[i][1] > graphicsData[i+1][1]) || (graphicsData[i-1][1] > graphicsData[i][1] && graphicsData[i][1] < graphicsData[i+1][1])))
            {
                canvas.setColor(Color.RED);
                flag = false;
            }
            else if (markPoint(graphicsData[i][1]))
                canvas.setColor(Color.BLUE);
            else
                canvas.setColor(Color.BLACK);

            GeneralPath path = new GeneralPath();
            Point2D.Double center = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
            path.moveTo(center.x, center.y + 5);
            path.lineTo(center.x + 5, center.y);
            path.lineTo(center.x, center.y - 5);
            path.lineTo(center.x - 5, center.y);
            path.lineTo(center.x, center.y + 5);
            canvas.draw(path);
            if (flag == false)
            {
                DecimalFormat tempX = new DecimalFormat("##.##");
                DecimalFormat tempY = new DecimalFormat("##.##");
                FontRenderContext context = canvas.getFontRenderContext();
                Rectangle2D bounds = axisFont.getStringBounds("Экстремум", context);
                Point2D.Double labelPos = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
                canvas.drawString("Экстремум", (float) labelPos.getX() + 5, (float) (labelPos.getY() - bounds.getY()));
                canvas.drawString("("+tempX.format(graphicsData[i][0])+"; "+ tempY.format(graphicsData[i][1])+")", (float) labelPos.getX() + 5, (float) (labelPos.getY() - bounds.getY()) - 20);
            }
        }
    }
    // Метод, обеспечивающий отображение осей координат
    protected void paintAxis(Graphics2D canvas)
    {
        // Установить особое начертание для осей
        canvas.setStroke(axisStroke);
        canvas.setColor(Color.BLACK);
        canvas.setPaint(Color.BLACK);
        canvas.setFont(axisFont);
        // Создать объект контекста отображения текста - для получения характеристик устройства (экрана)
        FontRenderContext context = canvas.getFontRenderContext();
        if (minX<=0.0 && maxX>=0.0)
        {
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(0, maxY);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX()+5, arrow.getCurrentPoint().getY()+20);
            arrow.lineTo(arrow.getCurrentPoint().getX()-10, arrow.getCurrentPoint().getY());
            arrow.closePath();
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку
            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);
            canvas.drawString("y", (float)labelPos.getX() + 10, (float)(labelPos.getY() - bounds.getY()));
        }

        if (minY<=0.0 && maxY>=0.0)
        {
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0), xyToPoint(maxX, 0)));
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX()-20, arrow.getCurrentPoint().getY()-5);
            arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY()+10);
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);
            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);
            canvas.drawString("x", (float)(labelPos.getX() -
                    bounds.getWidth() - 10), (float)(labelPos.getY() + bounds.getY()));
        }
    }
    // Метод-помощник, осуществляющий преобразование координат.
    protected Point2D.Double xyToPoint(double x, double y)
    {
        double deltaX = x - minX;
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX*scale, deltaY*scale);
    }
    // Метод-помощник, возвращающий экземпляр класса Point2D.Double
    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY)
    {
        // Инициализировать новый экземпляр точки
        Point2D.Double dest = new Point2D.Double();
        // Задать еѐ координаты как координаты существующей точки + заданные смещения
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }
    protected Point2D.Double pointToXY(int x, int y)
    {
        Point2D.Double p = new Point2D.Double();
        if (!transform)
        {
            p.x = x / scale + minX;
            int q = (int) xyToPoint(0, 0).y;
            p.y = maxY - maxY * ((double) y / (double) q);
        } else
            {
            if (!zoom)
            {
                p.y = -x / scale + (maxY);
                p.x = -y / scale + maxX;
            }
            else
                {
                p.y = -x / scaleY + (maxY);
                p.x = -y / scaleX + maxX;
            }
        }
        return p;
    }


    public class MouseMotionHandler implements MouseMotionListener, MouseListener {
        private double comparePoint(Point p1, Point p2) {
            return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
        }

        private GraphPoint find(int x, int y) {
            GraphPoint smp = new GraphPoint();
            GraphPoint smp2 = new GraphPoint();
            double r, r2 = 1000;
            for (int i = 0; i < graphicsData.length; i++) {
                Point p = new Point();
                p.x = x;
                p.y = y;
                Point p2 = new Point();
                p2.x = graphicsDataI[i][0];
                p2.y = graphicsDataI[i][1];
                r = comparePoint(p, p2);
                if (r < 7.0) {
                    smp.x = graphicsDataI[i][0];
                    smp.y = graphicsDataI[i][1];
                    smp.xd = graphicsData[i][0];
                    smp.yd = graphicsData[i][1];
                    smp.n = i;
                    if (r < r2) {
                        smp2 = smp;
                    }
                    return smp2;
                }
            }
            return null;
        }

        public void mouseMoved(MouseEvent ev) {
            GraphPoint smp;
            smp = find(ev.getX(), ev.getY());
            if (smp != null)
                SMP = smp;
            else SMP = null;
            repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (selMode) {
                if (!transform)
                    rect.setFrame(mausePX, mausePY, e.getX() - rect.getX(),
                            e.getY() - rect.getY());
                else
                    rect.setFrame(-mausePY + getHeight(), mausePX, -e.getY()
                            + mausePY, e.getX() - mausePX);
                repaint();
            }
            if (dragMode) {
                if (!transform) {
                    if (pointToXY(e.getX(), e.getY()).y < maxY && pointToXY(e.getX(), e.getY()).y > minY) {
                        graphicsData[SMP.n][1] = pointToXY(e.getX(), e.getY()).y;
                        SMP.yd = pointToXY(e.getX(), e.getY()).y;
                        SMP.y = e.getY();
                    }
                } else {
                    if (pointToXY(e.getX(), e.getY()).y < maxY && pointToXY(e.getX(), e.getY()).y > minY) {
                        graphicsData[SMP.n][1] = pointToXY(e.getX(), e.getY()).y;
                        SMP.yd = pointToXY(e.getX(), e.getY()).y;
                        SMP.x = e.getX();
                    }
                }
                repaint();
            }
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {
            if (e.getButton() != 3)
                return;

            try
            {
                zone = stack.pop();
            }
            catch (EmptyStackException err)
            {
            }

            if (stack.empty())
                zoom = false;
            repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            selMode = true;
            mausePX = e.getX();
            mausePY = e.getY();
            rect.setFrame(e.getX(), e.getY(), 0, 0);
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            rect.setFrame(0, 0, 0, 0);
            if (e.getButton() != 1)
            {
                repaint();
                return;
            }
            if (selMode)
            {
                if (!transform)
                {
                    if (e.getX() <= mausePX || e.getY() <= mausePY)
                        return;
                    int eY = e.getY();
                    int eX = e.getX();
                    if (eY > getHeight())
                        eY = getHeight();
                    if (eX > getWidth())
                        eX = getWidth();
                    double MAXX = pointToXY(eX, 0).x;
                    double MINX = pointToXY(mausePX, 0).x;
                    double MAXY = pointToXY(0, mausePY).y;
                    double MINY = pointToXY(0, eY).y;
                    stack.push(zone);
                    zone = new Zone();
                    zone.use = true;
                    zone.MAXX = MAXX;
                    zone.MINX = MINX;
                    zone.MINY = MINY;
                    zone.MAXY = MAXY;
                    selMode = false;
                    zoom = true;
                } else
                    {
                    if (pointToXY(mausePX, 0).y <= pointToXY(e.getX(), 0).y
                            || pointToXY(0, e.getY()).x <= pointToXY(0, mausePY).x)
                        return;
                    int eY = e.getY();
                    int eX = e.getX();
                    if (eY < 0)
                        eY = 0;
                    if (eX > getWidth())
                        eX = getWidth();
                    stack.push(zone);
                    zone = new Zone();
                    zone.use = true;
                    zone.MAXY = pointToXY(mausePX, 0).y;
                    zone.MAXX = pointToXY(0, eY).x;
                    zone.MINX = pointToXY(0, mausePY).x;
                    zone.MINY = pointToXY(eX, 0).y;
                    selMode = false;
                    zoom = true;
                }

            }
            repaint();
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }
}

