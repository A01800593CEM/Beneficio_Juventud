import { PipeTransform, Injectable, NotFoundException } from '@nestjs/common';
import { CategoriesService } from '../../categories/categories.service';
import { Category } from '../../categories/entities/category.entity';

@Injectable()
export class CategoriesByNamePipe implements PipeTransform {
  constructor(private readonly categoriesService: CategoriesService) {}

  async transform(value: string[] | string): Promise<Category[]> {
    const names = Array.isArray(value) ? value : [value];
    
    const categories = await this.categoriesService.findByNames(names);

    if (categories.length !== names.length) {
      throw new NotFoundException('Some categories were not found');
    }

    return categories;
  }
}
