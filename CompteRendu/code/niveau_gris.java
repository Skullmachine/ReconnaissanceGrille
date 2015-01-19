//Conversion en niveau de gris

int[][] gray = new int[width][height];

for (int y=0;y<height;y++)
{
	for (int x=0;x<width;x++)
	{
		int rgb = img0.getRGB(x, y);
		int r = (rgb >>16 ) & 0xFF;
		int g = (rgb >> 8 ) & 0xFF;
		int b = rgb & 0xFF;
		gray[x][y]=(299*r + 587*g + 114*b)/1000;
	}
}